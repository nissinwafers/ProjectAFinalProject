module com.example.finalprojecta {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.finalprojecta to javafx.fxml;
    exports com.example.finalprojecta;
}