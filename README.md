# Project Overview
This is a project powered by Maven(JavaFX+XML) and MySQL80.

## Frontend Implementation guidelines
The frontend of this project is implemented using JavaFX and XML. The user interface is designed with FXML files, which define the layout and components of the application. JavaFX provides a rich set of UI controls and features to create a responsive and interactive user experience.

**File paths for frontend components:**
- FXML+CSS files: `desktop-app\src\main\resources\com\ccinfoms17grp2\ui\`
- Java controller files: `desktop-app\src\main\java\com\ccinfoms17grp2\ui\controller\`

## Backend Implementation guidelines
The backend of this project is built using Java and MySQL80. Java is used to handle the business logic, data processing, and communication with the database. MySQL80 serves as the database management system, storing and managing the application's data.

**File paths for backend components:**
- MySQL80 Schema files: `backend-service\schemas\`
- Java service files: `backend-service\src\main\java\com\ccinfoms17grp2\service\`
- Java model files: `backend-service\src\main\java\com\ccinfoms17grp2\models\`
- Java data access object files: `backend-service\src\main\java\com\ccinfoms17grp2\dao\`
- Java utility files: `backend-service\src\main\java\com\ccinfoms17grp2\utils\`
- Java implementation files: `desktop-app\src\main\java\com\ccinfoms17grp2\dao\impl\`

## UI/UX Design
The UI/UX design of this project focuses on providing an intuitive and user-friendly experience. The design principles include simplicity, consistency, and responsiveness. The application is designed to be visually appealing while ensuring ease of navigation and accessibility for users.

**Patient Portal UI/UX Design:**
- The patient portal allows users to book appointments, view medical records, and communicate with healthcare providers.
- The design emphasizes clear navigation, easy access to information, and a seamless user experience.

```
Login -> Homepage
            -> Appointments List
                -> Book Appointment
                    -> Select Date & Time
                    -> Select Own Location
                    -> Select Multiple Specializations
                -> View Appointment Details
            -> Consultation Records
                -> View Records Details
```

**Doctor Portal UI/UX Design:**
- The doctor portal provides functionalities for managing patient appointments, viewing medical histories, and updating treatment plans.
- The design prioritizes efficiency, clarity, and quick access to critical information for healthcare professionals.

**Admin Portal UI/UX Design:**
- The admin portal provides functionalities for managing patient records, appointments, and system settings.
- The design focuses on efficiency, clarity, and ease of use for administrative tasks.
