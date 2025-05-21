package com.example.finalprojecta;

import databasemodel.DatabaseConnector;
import databasemodel.UserSQL;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdSubjectManagerController implements Initializable {

    @FXML
    private TableColumn<Subject, String> a_courseCodeColumn;

    @FXML
    private ComboBox<String> a_courseComboBox;

    @FXML
    private TableColumn<Subject, String> a_descriptiveTitleColumn;

    @FXML
    private TableColumn<Subject, String> a_prerequisitesColumn;

    @FXML
    private ComboBox<String> a_schoolYearComboBox;

    @FXML
    private ComboBox<Integer> a_programYearComboBox;

    @FXML
    private ComboBox<String> a_semesterComboBox;

    @FXML
    private TableColumn<Subject, Integer> a_totalUnitsColumn;

    @FXML
    private Button addSubjectButton;

    @FXML
    private Button assignButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button removeSubjectButton;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Button subjManagerButton;

    @FXML
    private TableView<Subject> subjectInputTableView;

    @FXML
    private Button viewStudentsButton;

    @FXML
    private Button saveButton;

    @FXML
    private TextField inputTextField;

    @FXML
    private Button filterButton;

    @FXML
    private Button clearFilterButton;

    // Reference to MainProject for navigation
    private MainProject main;

    // Observable list to hold subjects
    private ObservableList<Subject> subjectList = FXCollections.observableArrayList();

    // Observable list to hold filtered subjects
    private ObservableList<Subject> filteredSubjectList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        a_courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        a_descriptiveTitleColumn.setCellValueFactory(new PropertyValueFactory<>("descriptiveTitle"));
        a_totalUnitsColumn.setCellValueFactory(new PropertyValueFactory<>("totalUnits"));
        a_prerequisitesColumn.setCellValueFactory(new PropertyValueFactory<>("prerequisites"));

        // Make table editable
        subjectInputTableView.setEditable(true);

        // Set up editable columns
        a_courseCodeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        a_descriptiveTitleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        a_totalUnitsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        a_prerequisitesColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // Set the items to the table
        subjectInputTableView.setItems(subjectList);

        // Initialize ComboBoxes
        initializeComboBoxes();

        // Set up button event handlers
        setupButtonHandlers();

        // Set up event handler for save button
        saveButton.setOnAction(event -> handleSaveButton());

        // Set up combo box change listeners for automatic filtering
        setupFilterListeners();
    }

    private void initializeComboBoxes() {
        // Initialize course combo box
        a_courseComboBox.setItems(FXCollections.observableArrayList(
                "BSIT"
        ));

        // Initialize semester combo box
        a_semesterComboBox.setItems(FXCollections.observableArrayList(
                "1st Semester", "2nd Semester", "Summer"
        ));

        // Initialize program year combo box
        a_programYearComboBox.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4
        ));

        // Initialize school year combo box
        a_schoolYearComboBox.setItems(FXCollections.observableArrayList("2024-2025"));
        a_schoolYearComboBox.getSelectionModel().selectFirst(); // Optional: preselect
    }

    private void setupButtonHandlers() {
        // Set up event handler for the Assign Teacher button
        assignButton.setOnAction(event -> navigateToAssignTeacher());

        // Add subject button
        addSubjectButton.setOnAction(event -> handleAddSubject());

        // Remove subject button
        removeSubjectButton.setOnAction(event -> handleRemoveSubject());

        // Set up Filter button
        if (filterButton != null) {
            filterButton.setOnAction(event -> loadFilteredSubjects());
        }

        // Set up Clear Filter button
        if (clearFilterButton != null) {
            clearFilterButton.setOnAction(event -> clearFilters());
        }

        // Set up logout button
        logoutButton.setOnAction(event -> handleLogout());
    }

    private void setupFilterListeners() {
        // Add listeners to automatically filter when combo box selections change
        a_courseComboBox.setOnAction(event -> loadFilteredSubjects());
        a_programYearComboBox.setOnAction(event -> loadFilteredSubjects());
        a_semesterComboBox.setOnAction(event -> loadFilteredSubjects());
        a_schoolYearComboBox.setOnAction(event -> loadFilteredSubjects());
    }

    @FXML
    private void handleAddSubject() {
        String input = inputTextField.getText().trim();
        System.out.println("[DEBUG] handleAddSubject called. Input: '" + input + "'");
        if (input.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter subject details in the input field.");
            return;
        }

        try {
            // Parse input format: CourseCode,DescriptiveTitle,TotalUnits,Prerequisites
            String[] parts = input.split(",");
            System.out.println("[DEBUG] Parsed input parts: " + java.util.Arrays.toString(parts));
            if (parts.length < 3) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input",
                        "Please enter the subject details in the format: CourseCode,DescriptiveTitle,TotalUnits,Prerequisites");
                return;
            }

            String courseCode = parts[0].trim();
            String descriptiveTitle = parts[1].trim();
            int totalUnits = Integer.parseInt(parts[2].trim());
            String prerequisites = parts.length > 3 ? parts[3].trim() : "";

            System.out.println("[DEBUG] Creating Subject: courseCode=" + courseCode + ", descriptiveTitle=" + descriptiveTitle + ", totalUnits=" + totalUnits + ", prerequisites=" + prerequisites);

            // Create new subject and add to list
            Subject subject = new Subject(courseCode, descriptiveTitle, totalUnits, prerequisites);
            subjectList.add(subject);
            System.out.println("[DEBUG] Subject added to subjectList. List size: " + subjectList.size());

            // Clear input field
            inputTextField.clear();

        } catch (NumberFormatException e) {
            System.out.println("[DEBUG] NumberFormatException: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please ensure the total units is a valid number.");
        }
    }

    @FXML
    private void handleRemoveSubject() {
        Subject selectedSubject = subjectInputTableView.getSelectionModel().getSelectedItem();
        if (selectedSubject == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a subject to remove.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Removal");
        confirmDialog.setHeaderText("Remove Subject");
        confirmDialog.setContentText("Are you sure you want to remove " + selectedSubject.getCourseCode() + "?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            subjectList.remove(selectedSubject);
        }
    }

    @FXML
    private void handleSaveButton() {
        System.out.println("[DEBUG] handleSaveButton called.");
        // Validate selections
        if (a_courseComboBox.getValue() == null ||
                a_semesterComboBox.getValue() == null ||
                a_programYearComboBox.getValue() == null ||
                a_schoolYearComboBox.getValue() == null) {

            System.out.println("[DEBUG] Missing selection in ComboBoxes.");
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select Course, Semester, Year, and School Year before saving.");
            return;
        }

        if (subjectList.isEmpty()) {
            System.out.println("[DEBUG] subjectList is empty. Nothing to save.");
            showAlert(Alert.AlertType.WARNING, "No Subjects",
                    "There are no subjects to save. Please add subjects first.");
            return;
        }

        // Save subjects to database
        saveSubjectsToDatabase();
    }

    private void saveSubjectsToDatabase() {
        System.out.println("[DEBUG] saveSubjectsToDatabase called. subjectList size: " + subjectList.size());
        try (Connection connection = DatabaseConnector.getConnection()) {
            System.out.println("[DEBUG] Database connection established.");
            String sql = "INSERT INTO SubjectManagerFile " +
                    "(SMFCOURSECODE, SMFDESCTITLE, SMFTOTALUNITS, SMFPREREQUISITES, " +
                    "SMFCOURSE, SMFPROGYEAR, SMFSEMESTER, SMFSCHOOLYR) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                int successCount = 0;

                for (Subject subject : subjectList) {
                    System.out.println("[DEBUG] Saving subject: " + subject);
                    pstmt.setString(1, subject.getCourseCode());
                    pstmt.setString(2, subject.getDescriptiveTitle());
                    pstmt.setString(3, String.valueOf(subject.getTotalUnits())); // Convert int to String
                    pstmt.setString(4, subject.getPrerequisites());
                    pstmt.setString(5, a_courseComboBox.getValue());
                    pstmt.setString(6, String.valueOf(a_programYearComboBox.getValue())); // Convert Integer to String
                    pstmt.setString(7, a_semesterComboBox.getValue());
                    pstmt.setString(8, a_schoolYearComboBox.getValue());

                    int result = pstmt.executeUpdate();
                    System.out.println("[DEBUG] executeUpdate result: " + result);
                    if (result > 0) {
                        successCount++;
                    }
                }

                if (successCount == subjectList.size()) {
                    System.out.println("[DEBUG] All subjects saved successfully.");
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            successCount + " subjects have been saved to the database successfully!");

                    // Clear the list after successful save
                    subjectList.clear();

                    // Refresh filtered view if filters are set
                    loadFilteredSubjects();
                } else {
                    System.out.println("[DEBUG] Partial save. Success count: " + successCount);
                    showAlert(Alert.AlertType.WARNING, "Partial Success",
                            "Only " + successCount + " out of " + subjectList.size() + " subjects were saved.");
                }
            }
        } catch (SQLException e) {
            System.out.println("[DEBUG] SQLException: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "An error occurred while saving subjects to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load subjects from database based on filter criteria
     */
    private void loadFilteredSubjects() {
        // Get filter values from comboboxes
        String course = a_courseComboBox.getValue();
        Integer programYear = a_programYearComboBox.getValue();
        String semester = a_semesterComboBox.getValue();
        String schoolYear = a_schoolYearComboBox.getValue();

        // Clear the current filtered list
        filteredSubjectList.clear();

        // Build SQL query with dynamic WHERE clause
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT SMFCOURSECODE, SMFDESCTITLE, SMFTOTALUNITS, SMFPREREQUISITES ")
                .append("FROM SubjectManagerFile WHERE 1=1 ");

        // Add conditions based on selected filters
        if (course != null && !course.isEmpty()) {
            sqlBuilder.append("AND SMFCOURSE = ? ");
        }

        if (programYear != null) {
            sqlBuilder.append("AND SMFPROGYEAR = ? ");
        }

        if (semester != null && !semester.isEmpty()) {
            sqlBuilder.append("AND SMFSEMESTER = ? ");
        }

        if (schoolYear != null && !schoolYear.isEmpty()) {
            sqlBuilder.append("AND SMFSCHOOLYR = ? ");
        }

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sqlBuilder.toString())) {

            // Set parameter values for the prepared statement
            int paramIndex = 1;

            if (course != null && !course.isEmpty()) {
                pstmt.setString(paramIndex++, course);
            }

            if (programYear != null) {
                pstmt.setString(paramIndex++, String.valueOf(programYear));
            }

            if (semester != null && !semester.isEmpty()) {
                pstmt.setString(paramIndex++, semester);
            }

            if (schoolYear != null && !schoolYear.isEmpty()) {
                pstmt.setString(paramIndex++, schoolYear);
            }

            // Execute query and process results
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String courseCode = rs.getString("SMFCOURSECODE");
                    String descriptiveTitle = rs.getString("SMFDESCTITLE");
                    int totalUnits = Integer.parseInt(rs.getString("SMFTOTALUNITS"));
                    String prerequisites = rs.getString("SMFPREREQUISITES");

                    Subject subject = new Subject(courseCode, descriptiveTitle, totalUnits, prerequisites);
                    filteredSubjectList.add(subject);
                }

                // Update the table view with filtered results
                subjectInputTableView.setItems(filteredSubjectList);

                // Update UI with filter information
                String filterInfo = buildFilterInfoString(course, programYear, semester, schoolYear);
                updateFilterInfoLabel(filterInfo);

            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build a string describing the current filters
     */
    private String buildFilterInfoString(String course, Integer progYear, String semester, String schoolYear) {
        StringBuilder info = new StringBuilder("Filters: ");
        boolean hasFilters = false;

        if (course != null && !course.isEmpty()) {
            info.append("Course: ").append(course);
            hasFilters = true;
        }

        if (progYear != null) {
            if (hasFilters) info.append(", ");
            info.append("Year: ").append(progYear);
            hasFilters = true;
        }

        if (semester != null && !semester.isEmpty()) {
            if (hasFilters) info.append(", ");
            info.append("Semester: ").append(semester);
            hasFilters = true;
        }

        if (schoolYear != null && !schoolYear.isEmpty()) {
            if (hasFilters) info.append(", ");
            info.append("School Year: ").append(schoolYear);
            hasFilters = true;
        }

        return hasFilters ? info.toString() : "No filters applied";
    }

    private void updateFilterInfoLabel(String filterInfo) {
        // Update label if you have one in your FXML
        // filterInfoLabel.setText(filterInfo);

        // For now just update the stage title to show filter info
        Stage stage = (Stage) subjectInputTableView.getScene().getWindow();
        stage.setTitle("Subject Manager - " + filterInfo);
    }

    /**
     * Clear all filters and reset view to adding new subjects
     */
    private void clearFilters() {
        // Clear combobox selections
        a_courseComboBox.getSelectionModel().clearSelection();
        a_programYearComboBox.getSelectionModel().clearSelection();
        a_semesterComboBox.getSelectionModel().clearSelection();
        a_schoolYearComboBox.getSelectionModel().clearSelection();

        // Switch back to the input list
        subjectInputTableView.setItems(subjectList);

        // Reset filter info
        Stage stage = (Stage) subjectInputTableView.getScene().getWindow();
        stage.setTitle("Subject Manager");
    }

    private void navigateToAssignTeacher() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-assignteacher.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) assignButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to load admin-assignteacher.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Logout Error",
                    "Failed to logout: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setSubjectMain(MainProject main) {
        this.main = main;
    }
}

