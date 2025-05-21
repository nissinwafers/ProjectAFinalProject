package com.example.finalprojecta;

import databasemodel.AssignedTeacherSQL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class AdAssignTeacherController implements Initializable {
    @FXML
    private Button assignDashButton;

    @FXML
    private Button assignTeacherButton;

    @FXML
    private TableView<AssignedTeacherModel> assignTeacherTableView;

    @FXML
    private TableColumn<AssignedTeacherModel, String> courseCodeColumn;

    @FXML
    private TextField courseTextField;

    @FXML
    private TableColumn<AssignedTeacherModel, String> descriptiveTitleColumn;

    @FXML
    private Button logoutButton;

    @FXML
    private TableColumn<AssignedTeacherModel, String> professorColumn;

    @FXML
    private Button removeTeacherButton;

    @FXML
    private Button saveTeacherButton;

    @FXML
    private ComboBox<String> schoolYearComboBox;

    @FXML
    private ComboBox<String> semesterComboBix;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Button subjectDashButton;

    @FXML
    private Button viewDashbUtton;

    @FXML
    private ComboBox<String> yearComboBix;

    @FXML
    private TextField descriptiveTitleField;

    @FXML
    private TextField professorField;

    // ObservableList to hold our table data
    private ObservableList<AssignedTeacherModel> assignedTeacherList = FXCollections.observableArrayList();

    // Database handler
    private AssignedTeacherSQL assignedTeacherSQL;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize database handler
        assignedTeacherSQL = new AssignedTeacherSQL();

        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        descriptiveTitleColumn.setCellValueFactory(new PropertyValueFactory<>("descriptiveTitle"));
        professorColumn.setCellValueFactory(new PropertyValueFactory<>("professor"));

        semesterComboBix.setItems(FXCollections.observableArrayList("1st Semester", "2nd Semester", "Summer"));
        yearComboBix.setItems(FXCollections.observableArrayList("1st Year", "2nd Year", "3rd Year", "4th Year"));

        schoolYearComboBox.setItems(FXCollections.observableArrayList("2024-2025"));
        schoolYearComboBox.getSelectionModel().selectFirst();

        semesterComboBix.getSelectionModel().selectFirst();
        yearComboBix.getSelectionModel().selectFirst();
        schoolYearComboBox.getSelectionModel().select("2024-2025");

        loadAssignedTeachers();

        setupButtonHandlers();
    }

    private void loadAssignedTeachers() {
        try {
            String semester = semesterComboBix.getValue();
            String year = yearComboBix.getValue();
            String schoolYear = schoolYearComboBox.getValue();

            // Load data from database based on filters
            if (semester != null && year != null && schoolYear != null) {
                assignedTeacherList = assignedTeacherSQL.getAssignedTeachers(semester, year, schoolYear);
            } else {
                assignedTeacherList = assignedTeacherSQL.getAllAssignedTeachers();
            }

            //if way data available, kani mogawas
            if (assignedTeacherList.isEmpty()) {
                assignedTeacherList.add(new AssignedTeacherModel("CS101", "Introduction to Programming", "Prof. Smith"));
                assignedTeacherList.add(new AssignedTeacherModel("CS202", "Data Structures", "Prof. Johnson"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not load data from database: " + e.getMessage());
        }

        assignTeacherTableView.setItems(assignedTeacherList);
    }

    //navigation buttons switching scenes
    private void setupButtonHandlers() {

        subjectDashButton.setOnAction(event -> navigateToScene("admin-subjmanager.fxml"));
        assignTeacherButton.setOnAction(event -> handleAssignTeacher());
        removeTeacherButton.setOnAction(event -> handleRemoveTeacher());
        saveTeacherButton.setOnAction(event -> handleSaveButton());
        logoutButton.setOnAction(event -> handleLogout());

        assignTeacherTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displaySelectedTeacher(newValue);
                    }
                }
        );

        semesterComboBix.setOnAction(event -> loadAssignedTeachers());
        yearComboBix.setOnAction(event -> loadAssignedTeachers());
        schoolYearComboBox.setOnAction(event -> loadAssignedTeachers());
    }

    private void displaySelectedTeacher(AssignedTeacherModel teacher) {
        courseTextField.setText(teacher.getCourseCode());
        descriptiveTitleField.setText(teacher.getDescriptiveTitle());
        professorField.setText(teacher.getProfessor());
    }

    @FXML
    private void handleAssignTeacher() {
        String courseCode = courseTextField.getText().trim();
        String descriptiveTitle = descriptiveTitleField.getText().trim();
        String professor = professorField.getText().trim();

        if (courseCode.isEmpty() || descriptiveTitle.isEmpty() || professor.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required!");
            return;
        }

        AssignedTeacherModel newAssignment = new AssignedTeacherModel(
                courseCode, descriptiveTitle, professor
        );

        assignedTeacherList.add(newAssignment);
        assignTeacherTableView.setItems(assignedTeacherList);

        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher assigned successfully!");
    }

    @FXML
    private void handleRemoveTeacher() {
        AssignedTeacherModel selectedTeacher = assignTeacherTableView.getSelectionModel().getSelectedItem();

        if (selectedTeacher == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a teacher to remove!");
            return;
        }

        assignedTeacherList.remove(selectedTeacher);
        assignTeacherTableView.setItems(assignedTeacherList);

        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher removed successfully!");
    }

    @FXML
    private void handleSaveButton() {
        try {
            String semester = semesterComboBix.getValue();
            String year = yearComboBix.getValue();
            String schoolYear = schoolYearComboBox.getValue();

            if (semester == null || year == null || schoolYear == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please select semester, year, and school year!");
                return;
            }

            boolean success = assignedTeacherSQL.saveAllTeachers(
                    assignedTeacherList, semester, year, schoolYear);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "All teachers saved to database successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save all teachers to database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not save to database: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Navigate to login screen
            navigateToScene("login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void clearFields() {
        courseTextField.clear();
        descriptiveTitleField.clear();
        professorField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) subjectDashButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate to " + fxmlFile + ": " + e.getMessage());
        }
    }
}