package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.BranchService;
import com.ccinfoms17grp2.services.DoctorService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DoctorBranchAssignmentController implements Initializable {

    @FXML
    private Label doctorNameLabel;
    @FXML
    private ListView<Branch> availableBranchesListView;
    @FXML
    private ListView<Branch> assignedBranchesListView;
    @FXML
    private Button assignButton;
    @FXML
    private Button unassignButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private final ServiceRegistry services;
    private final User currentUser;
    private final Doctor doctor;
    private final ExecutorService executorService;
    private List<Branch> allBranches;
    private List<Branch> assignedBranches;

    public DoctorBranchAssignmentController(ServiceRegistry services, User currentUser, Doctor doctor) {
        this.services = services;
        this.currentUser = currentUser;
        this.doctor = doctor;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        doctorNameLabel.setText("Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
        
        availableBranchesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        assignedBranchesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        assignButton.setDisable(true);
        unassignButton.setDisable(true);
        
        availableBranchesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            assignButton.setDisable(newVal == null);
        });
        
        assignedBranchesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            unassignButton.setDisable(newVal == null);
        });
        
        loadBranches();
    }

    private void loadBranches() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                BranchService branchService = services.getBranchService();
                DoctorService doctorService = services.getDoctorService();
                
                allBranches = branchService.listBranches();
                assignedBranches = doctorService.getBranchesForDoctor(doctor.getDoctorId());
                
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            updateLists();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load branches", task.getException());
        });
        
        executorService.execute(task);
    }

    private void updateLists() {
        List<Integer> assignedIds = assignedBranches.stream()
            .map(Branch::getBranchId)
            .collect(Collectors.toList());
        
        List<Branch> available = allBranches.stream()
            .filter(b -> !assignedIds.contains(b.getBranchId()))
            .collect(Collectors.toList());
        
        availableBranchesListView.getItems().clear();
        availableBranchesListView.getItems().addAll(available);
        
        assignedBranchesListView.getItems().clear();
        assignedBranchesListView.getItems().addAll(assignedBranches);
    }

    @FXML
    private void handleAssign() {
        List<Branch> selected = availableBranchesListView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) return;
        
        assignedBranches.addAll(selected);
        updateLists();
    }

    @FXML
    private void handleUnassign() {
        List<Branch> selected = assignedBranchesListView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) return;
        
        assignedBranches.removeAll(selected);
        updateLists();
    }

    @FXML
    private void handleSave() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                DoctorService doctorService = services.getDoctorService();
                List<Integer> branchIds = assignedBranches.stream()
                    .map(Branch::getBranchId)
                    .collect(Collectors.toList());
                
                doctorService.assignDoctorToBranches(doctor.getDoctorId(), branchIds);
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", "Branch assignments saved successfully");
            closeForm();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to save branch assignments", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
