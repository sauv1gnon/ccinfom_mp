package com.ccinfoms17grp2.ui;

public enum UiView {
    LOGIN("/com/ccinfoms17grp2/ui/login.fxml", "Login"),
    PATIENT_REGISTRATION("/com/ccinfoms17grp2/ui/patient-registration.fxml", "Patient Registration"),
    ADMIN_DASHBOARD("/com/ccinfoms17grp2/ui/admin-dashboard.fxml", "Admin Dashboard"),
    ADMIN_USERS("/com/ccinfoms17grp2/ui/admin-users.fxml", "Manage Users"),
    ADMIN_PATIENTS("/com/ccinfoms17grp2/ui/admin-patients.fxml", "Manage Patients"),
    ADMIN_DOCTORS("/com/ccinfoms17grp2/ui/admin-doctors.fxml", "Manage Doctors"),
    ADMIN_SPECIALIZATIONS("/com/ccinfoms17grp2/ui/admin-specializations.fxml", "Manage Specializations"),
    ADMIN_BRANCHES("/com/ccinfoms17grp2/ui/admin-branches.fxml", "Manage Branches"),
    ADMIN_APPOINTMENTS("/com/ccinfoms17grp2/ui/admin-appointments.fxml", "Manage Appointments"),
    ADMIN_CONSULTATIONS("/com/ccinfoms17grp2/ui/admin-consultations.fxml", "Manage Consultations"),
    ADMIN_QUEUES("/com/ccinfoms17grp2/ui/admin-queues.fxml", "Manage Queues"),
    PATIENT_HOME("/com/ccinfoms17grp2/ui/patient-home.fxml", "Patient Homepage"),
    DOCTOR_DASHBOARD("/com/ccinfoms17grp2/ui/doctor-dashboard.fxml", "Doctor Dashboard");

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
