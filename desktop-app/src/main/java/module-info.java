module com.ccinfoms.desktopapp {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires java.logging;
	requires java.naming;

	exports com.ccinfoms17grp2;
	exports com.ccinfoms17grp2.models;
	exports com.ccinfoms17grp2.utils;

	opens com.ccinfoms17grp2 to javafx.graphics;
}
