module com.example.finalprojecta {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires com.dlsc.formsfx;

    opens com.example.finalprojecta to javafx.fxml;
    exports com.example.finalprojecta;
    exports databasemodel;
    opens databasemodel to javafx.fxml;
}