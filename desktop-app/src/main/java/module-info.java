module com.ccinfoms17grp2 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ccinfoms17grp2 to javafx.fxml;
    exports com.ccinfoms17grp2;
}
