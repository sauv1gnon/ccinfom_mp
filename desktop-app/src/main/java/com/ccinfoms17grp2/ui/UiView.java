package com.ccinfoms17grp2.ui;

public enum UiView {
    LOGIN("login.fxml", "Login"),
    PATIENT_REGISTRATION("patient-registration.fxml", "Patient Registration"),
    ADMIN_DASHBOARD("admin-dashboard.fxml", "Admin Dashboard"),
    ADMIN_USERS("admin-users.fxml", "Manage Users"),
    ADMIN_PATIENTS("admin-patients.fxml", "Manage Patients"),
    ADMIN_DOCTORS("admin-doctors.fxml", "Manage Doctors"),
    ADMIN_SPECIALIZATIONS("admin-specializations.fxml", "Manage Specializations"),
    ADMIN_BRANCHES("admin-branches.fxml", "Manage Branches"),
    ADMIN_APPOINTMENTS("admin-appointments.fxml", "Manage Appointments"),
    ADMIN_CONSULTATIONS("admin-consultations.fxml", "Manage Consultations"),
    ADMIN_QUEUES("admin-queues.fxml", "Manage Queues"),
    PATIENT_HOME("patient-home.fxml", "Patient Homepage"),
    DOCTOR_DASHBOARD("doctor-dashboard.fxml", "Doctor Dashboard");

    private final String resourcePath;
    private final String title;

    UiView(String resourcePath, String title) {
        this.resourcePath = resourcePath;
        this.title = title;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getTitle() {
        return title;
    }
}
