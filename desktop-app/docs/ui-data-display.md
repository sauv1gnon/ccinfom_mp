# UI Data Display Implementation Guide

This guide explains how the JavaFX desktop client pulls records from the SQL database and renders them on-screen using the DAO and service layers that already exist in the project.

Refer to [https://docs.google.com/document/d/1QbLOzret8yHOofVa-ulw5YguD2WsL-ESkT9qpEeBwzk/edit?tab=t.0]

## Layered Architecture Recap

```
JavaFX Controller → Service (validation, orchestration) → DAO (JDBC) → MySQL
```

- `ServiceRegistry` wires together the DAO implementations (`BranchJdbcDao`, `DoctorJdbcDao`, `SpecializationJdbcDao`, etc.) and exposes high-level services such as `DoctorService` and `SpecializationService`.
- Each `*Service` validates user input, invokes the DAO methods, and converts low-level failures into `ValidationException` that the UI can surface to the user.
- DAO implementations extend `AbstractJdbcDao`, which centralises connection handling through `DatabaseConnection` and wraps checked `SQLException` instances in the runtime `DaoException`.

Because the DAO layer is synchronous and blocking, all database calls must be executed off the JavaFX Application Thread. Use JavaFX `Task` or other concurrency helpers to avoid freezing the UI while records are fetched.

## Database Configuration

`DatabaseConnection` reads credentials from `desktop-app/src/main/resources/db.properties`. Ensure this file is bundled with the packaged application and points to the correct schema:

```properties
db.url=jdbc:mysql://localhost:3306/queue_system
db.user=app_user
db.password=secret
```

When you run the desktop client via `mvn clean javafx:run`, the configuration is loaded from the classpath automatically. Update the values to match your local setup or the deployment database.

## Bootstrapping Services in the UI

Create a single `ServiceRegistry` instance at application start-up and share it with controllers. The simplest approach is to register it on the `Application` subclass and provide convenient accessors.

```java
public class App extends Application {

    private final ServiceRegistry services = new ServiceRegistry();

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/primary.fxml"));
        loader.setControllerFactory(type -> {
            if (type == PrimaryController.class) {
                return new PrimaryController(services);
            }
            // Add other controllers here as needed.
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to instantiate controller: " + type, ex);
            }
        });
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Digital Queue and Appointment System");
        primaryStage.show();
    }
}
```

## Displaying Table Data (Example: Specializations)

1. **FXML View**  
   Define a `TableView` with the desired columns. Assign a controller that receives the `ServiceRegistry`.

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <?import javafx.scene.control.*?>
   <?import javafx.scene.layout.*?>
   <BorderPane xmlns="http://javafx.com/javafx/13" xmlns:fx="http://javafx.com/fxml/1"
               fx:controller="com.ccinfoms17grp2.ui.SpecializationController">
       <center>
           <TableView fx:id="specializationTable">
               <columns>
                   <TableColumn fx:id="codeColumn" text="Code" prefWidth="120"/>
                   <TableColumn fx:id="nameColumn" text="Name" prefWidth="260"/>
               </columns>
           </TableView>
       </center>
   </BorderPane>
   ```

2. **Controller Setup**  
   Retrieve the service from the registry, back the table with an `ObservableList`, and define `cellValueFactory` lambdas.

   ```java
   public class SpecializationController {

       @FXML private TableView<Specialization> specializationTable;
       @FXML private TableColumn<Specialization, String> codeColumn;
       @FXML private TableColumn<Specialization, String> nameColumn;

       private final SpecializationService specializationService;
       private final ObservableList<Specialization> items = FXCollections.observableArrayList();

       public SpecializationController(ServiceRegistry services) {
           this.specializationService = services.getSpecializationService();
       }

       @FXML
       private void initialize() {
           codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecializationCode()));
           nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecializationName()));
           specializationTable.setItems(items);
           loadData();
       }

       private void loadData() {
           Task<List<Specialization>> task = new Task<>() {
               @Override
               protected List<Specialization> call() {
                   return specializationService.listSpecializations();
               }
           };
           task.setOnSucceeded(evt -> {
               items.setAll(task.getValue());
           });
           task.setOnFailed(evt -> {
               UiUtils.showError("Load Failed",
                       "Unable to fetch specializations from the database.",
                       task.getException());
           });
           new Thread(task, "specialization-loader").start();
       }
   }
   ```

   - DAO operations run in the background thread (`Task`).
   - Once the data arrives, the UI list updates on the JavaFX thread because `Task` re-enters the UI thread before firing `setOnSucceeded`.
   - Errors bubble up through `DaoException` and are displayed using `UiUtils`.

3. **Refreshing Data After Mutations**  
   After creating or updating a record, call `loadData()` again or update the `ObservableList` manually with the returned entity from `create`/`update`.

## Wiring CRUD Actions

When exposing create, update, or delete actions in the UI:

1. Collect user input via form controls.
2. Construct the corresponding model (`Doctor`, `Patient`, etc.).
3. Invoke the appropriate service method inside a background task.
4. On success, update the observable list and show a confirmation via `UiUtils.showInformation`.
5. On failure, catch `ValidationException` for user errors and `DaoException` for infrastructure issues. Display both with actionable feedback.

```java
private void saveDoctor() {
    Doctor payload = buildDoctorFromForm(); // map UI fields into model
    Task<Doctor> task = new Task<>() {
        @Override
        protected Doctor call() {
            return payload.getDoctorId() == 0
                    ? doctorService.createDoctor(payload)
                    : doctorService.updateDoctor(payload);
        }
    };
    task.setOnSucceeded(e -> {
        Doctor refreshed = task.getValue();
        updateTable(refreshed);
        UiUtils.showInformation("Success", "Doctor record saved.");
    });
    task.setOnFailed(e -> handleFailure(task.getException()));
    new Thread(task, "doctor-save").start();
}
```

## Threading and Error Handling Checklist

- Never call DAO or service methods directly on the UI thread; wrap them in `Task`, `Service`, or use `CompletableFuture.supplyAsync`.
- Surface validation issues (`ValidationException`) to users with friendly messages.
- Log unexpected `DaoException` causes, but display generic guidance (e.g., “Check your connection settings”) to avoid overwhelming users.
- Use `UiUtils` helpers to keep alert handling consistent and to ensure dialogs display on the JavaFX thread.

## Integrating Additional Views

For new modules (patients, doctors, queues):

1. Decide on the data set and locate the matching service method (`listDoctors`, `findBySpecialization`, `listPatients`, etc.).
2. Create FXML with table columns bound to model properties.
3. Follow the controller pattern above: dependency inject the service, configure columns, load data asynchronously, and handle CRUD operations.
4. Reuse utility classes:
   - `DateTimeUtil` for formatting timestamps (`Doctor.createdAt`, `Appointment.appointmentDateTime`).
   - `UiUtils` for modal dialogs.
5. Route navigation through a central controller or tab pane that instantiates each feature controller once and keeps the shared `ServiceRegistry`.

## Troubleshooting

- **No data appears:** Confirm the MySQL database contains records and that `db.properties` matches the running instance. Enable finer logging at `DatabaseConnection` by configuring `java.util.logging` if needed.
- **“Failed to fetch ...” errors:** Inspect the underlying `SQLException` message wrapped by `DaoException`. Typical causes include missing tables, incorrect credentials, or network issues.
- **UI freezing:** Ensure every database call runs on a background thread. Review controller code for direct `service.list...()` calls executed during button handlers without `Task`.
- **ClassNotFoundException for JDBC driver:** Maven already includes `mysql-connector-j`; if you run outside Maven, add the driver jar to the JavaFX runtime module path.

Following this workflow keeps the UI responsive, centralises data validation, and makes it straightforward to plug additional SQL-backed screens into the JavaFX application.
