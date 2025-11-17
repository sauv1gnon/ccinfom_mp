package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;

public class PatientRegistrationController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField contactField;

    @FXML
    private ChoiceBox<Branch> branchChoice;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void initialize() {
        branchChoice.setConverter(new StringConverter<Branch>() {
            @Override
            public String toString(Branch branch) {
                if (branch == null) {
                    return "";
                }
                return branch.getBranchName() + " (" + branch.getBranchId() + ")";
            }

            @Override
            public Branch fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void handleSubmit() {
        if (services == null) {
            feedbackLabel.setText("Services unavailable");
            return;
        }
        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String contact = contactField.getText() == null ? "" : contactField.getText().trim();
        Branch branch = branchChoice.getValue();
        if (fullName.isEmpty() || email.isEmpty() || contact.isEmpty() || branch == null) {
            feedbackLabel.setText("Complete all required fields and choose a branch");
            return;
        }
        String[] nameParts = splitName(fullName);
        Patient patient = new Patient();
        patient.setFirstName(nameParts[0]);
        patient.setLastName(nameParts[1]);
        patient.setEmail(email);
        patient.setContactNumber(contact);
        try {
            Patient created = services.getPatientService().createPatient(patient);
            feedbackLabel.setText("Registered patient #" + created.getPatientId() + " for " + branch.getBranchName());
            clearForm();
        } catch (ValidationException ex) {
            feedbackLabel.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            feedbackLabel.setText("Unable to submit registration");
            UiUtils.showError("Registration failed", ex.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        navigator.show(UiView.LOGIN);
    }

    @Override
    public void setNavigator(SceneNavigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void setServices(ServiceRegistry services) {
        this.services = services;
        loadBranches();
    }

    @Override
    public void setSession(SessionContext session) {
    }

    @Override
    public void onDisplay() {
        feedbackLabel.setText("");
        loadBranches();
    }

    private void loadBranches() {
        if (services == null || branchChoice == null) {
            return;
        }
        List<Branch> branches = services.getBranchService().listBranches();
        branchChoice.getItems().setAll(branches);
        if (!branches.isEmpty() && branchChoice.getValue() == null) {
            branchChoice.getSelectionModel().selectFirst();
        }
    }

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        contactField.clear();
        notesArea.clear();
        branchChoice.getSelectionModel().clearSelection();
    }

    private String[] splitName(String fullName) {
        String normalized = fullName.trim();
        int index = normalized.lastIndexOf(' ');
        if (index <= 0) {
            return new String[] { normalized, normalized };
        }
        String first = normalized.substring(0, index).trim();
        String last = normalized.substring(index + 1).trim();
        if (first.isEmpty()) {
            first = last;
        }
        if (last.isEmpty()) {
            last = first;
        }
        return new String[] { first, last };
    }
}
