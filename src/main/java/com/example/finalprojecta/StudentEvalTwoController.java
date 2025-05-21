package com.example.finalprojecta;

import databasemodel.DatabaseConnector;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StudentEvalTwoController implements Initializable {

    @FXML
    private Button checkGradesButton;

    @FXML
    private TableColumn<SubjectRecommendation, String> courseCodeColumn;

    @FXML
    private TableColumn<SubjectRecommendation, String> descriptiveTitleColumn;

    @FXML
    private Button enrollButton;

    @FXML
    private Button evaluationButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button profileButton;

    @FXML
    private TableColumn<SubjectRecommendation, String> statusColumn;

    @FXML
    private TableView<SubjectRecommendation> subsToTakeTableView;

    @FXML
    private TableColumn<SubjectRecommendation, Integer> totalUnitsColumn;

    @FXML
    private TextField unitsValidationTextField;

    private DatabaseConnector dbConnection;
    private String username;
    private int totalUnits = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            dbConnection = new DatabaseConnector();

            courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
            descriptiveTitleColumn.setCellValueFactory(new PropertyValueFactory<>("descriptiveTitle"));
            totalUnitsColumn.setCellValueFactory(new PropertyValueFactory<>("totalUnits"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Make units validation field not editable
            unitsValidationTextField.setEditable(false);

            setupButtonHandlers();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error",
                    "Failed to initialize the controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Special handling for course with no prerequisites or when prerequisites can't be checked
    public void setStudentInfo(String username) {
        this.username = username;
        System.out.println("Setting student info for: " + username);

        try (Connection conn = dbConnection.getConnection()) {
            String query = "SELECT TOP 1 PSFPROGRAMYEAR, PSFSEMESTER FROM PreviousSubjectFile " +
                    "WHERE PSFUSERNAME = ? ORDER BY PSFNUM DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String year = rs.getString("PSFPROGRAMYEAR");
                String semester = rs.getString("PSFSEMESTER");

                System.out.println("Found student year: " + year + ", semester: " + semester);
                loadRecommendedSubjects("BSIT", year, semester, username);
            } else {
                System.out.println("No previous subjects found for student: " + username);
                // If no previous subjects are found, load first year, first semester subjects
                loadFirstYearFirstSemesterSubjects("BSIT", username);
                showAlert(Alert.AlertType.INFORMATION, "First Year Student",
                        "Recommending first year, first semester subjects.");
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not fetch student semester/year.");
            e.printStackTrace();
        }
    }

    private void loadFirstYearFirstSemesterSubjects(String program, String username) {
        ObservableList<SubjectRecommendation> recommendedSubjects = FXCollections.observableArrayList();
        totalUnits = 0;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM SubjectManagerFile WHERE SMFCOURSE = ? AND SMFPROGYEAR = ? AND SMFSEMESTER = ?")) {

            stmt.setString(1, program);
            stmt.setString(2, "1");  // First year
            stmt.setString(3, "1");  // First semester
            ResultSet rs = stmt.executeQuery();

            System.out.println("Loading first year, first semester subjects");
            while (rs.next()) {
                String courseCode = rs.getString("SMFCOURSECODE").trim();
                String title = rs.getString("SMFDESCTITLE").trim();
                int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));

                System.out.println("Found first year subject: " + courseCode + " - " + title);

                SubjectRecommendation subject = new SubjectRecommendation(courseCode, title, units, "To Take");
                recommendedSubjects.add(subject);
                totalUnits += units;
            }

            subsToTakeTableView.setItems(recommendedSubjects);
            unitsValidationTextField.setText(String.valueOf(totalUnits));

            System.out.println("Added " + recommendedSubjects.size() + " first year subjects");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error loading first year subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRecommendedSubjects(String program, String year, String semester, String username) {
        ObservableList<SubjectRecommendation> recommendedSubjects = FXCollections.observableArrayList();
        totalUnits = 0;

        try {
            // Load student grades for all subjects they've taken
            Map<String, Double> studentGrades = loadStudentGrades();

            // Debugging: Print out all loaded grades
            System.out.println("Student grades loaded: " + studentGrades.size());
            for (Map.Entry<String, Double> entry : studentGrades.entrySet()) {
                System.out.println("Course: " + entry.getKey() + ", Grade: " + entry.getValue());
            }

            // Check for failed subjects (subjects with grade > 3.0)
            List<SubjectRecommendation> failedSubjects = new ArrayList<>();
            for (Map.Entry<String, Double> entry : studentGrades.entrySet()) {
                String courseCode = entry.getKey().trim();
                double grade = entry.getValue();

                if (grade > 3.0) {
                    System.out.println("Found failed course: " + courseCode + " with grade: " + grade);

                    // Get subject details for this failed course
                    try (Connection conn = dbConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                                 "SELECT * FROM SubjectManagerFile WHERE SMFCOURSECODE = ?")) {

                        stmt.setString(1, courseCode);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            String title = rs.getString("SMFDESCTITLE").trim();
                            int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));

                            SubjectRecommendation failedSubject = new SubjectRecommendation(
                                    courseCode, title, units, "To Retake");
                            failedSubjects.add(failedSubject);
                            totalUnits += units;

                            System.out.println("Added failed subject: " + courseCode);
                        } else {
                            System.out.println("Failed to find details for course: " + courseCode);
                        }
                    }
                }
            }

            // Load all available subjects for the current year/semester
            List<SubjectRecommendation> availableSubjects = new ArrayList<>();

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM SubjectManagerFile WHERE SMFCOURSE = ? AND SMFPROGYEAR = ? AND SMFSEMESTER = ?")) {

                stmt.setString(1, program);
                stmt.setString(2, year);
                stmt.setString(3, semester);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Checking subjects for: " + program + ", Year: " + year + ", Semester: " + semester);
                int subjectCount = 0;

                while (rs.next()) {
                    subjectCount++;
                    String courseCode = rs.getString("SMFCOURSECODE").trim();
                    String title = rs.getString("SMFDESCTITLE").trim();
                    int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));
                    String prerequisites = rs.getString("SMFPREREQUISITES").trim();

                    System.out.println("Found subject: " + courseCode + " - " + title);
                    System.out.println("  Prerequisites: " + prerequisites);

                    // Skip if student already passed this subject
                    boolean alreadyPassed = false;
                    for (String takenCourse : studentGrades.keySet()) {
                        if (takenCourse.trim().equalsIgnoreCase(courseCode) &&
                                studentGrades.get(takenCourse) <= 3.0) {
                            alreadyPassed = true;
                            System.out.println("  Already passed: " + courseCode);
                            break;
                        }
                    }

                    if (alreadyPassed) {
                        continue;
                    }

                    // Check if prerequisites are met
                    boolean prereqsMet = true;

                    if (!prerequisites.equalsIgnoreCase("none") && !prerequisites.isBlank()) {
                        // Handle different delimiter formats
                        String[] prereqList;
                        if (prerequisites.contains(",")) {
                            prereqList = prerequisites.split(",");
                        } else if (prerequisites.contains(";")) {
                            prereqList = prerequisites.split(";");
                        } else {
                            prereqList = new String[]{prerequisites};
                        }

                        for (String prereq : prereqList) {
                            String cleanedPrereq = prereq.trim();
                            if (cleanedPrereq.isEmpty()) continue;

                            System.out.println("  Checking prereq: " + cleanedPrereq);

                            boolean prereqPassed = false;
                            for (String takenCourse : studentGrades.keySet()) {
                                if (takenCourse.trim().equalsIgnoreCase(cleanedPrereq)) {
                                    double grade = studentGrades.get(takenCourse);
                                    if (grade <= 3.0) {
                                        prereqPassed = true;
                                        System.out.println("  Prereq passed: " + cleanedPrereq);
                                        break;
                                    } else {
                                        System.out.println("  Prereq failed: " + cleanedPrereq + " with grade: " + grade);
                                    }
                                }
                            }

                            if (!prereqPassed) {
                                prereqsMet = false;
                                System.out.println("  Prerequisite not met: " + cleanedPrereq);
                                break;
                            }
                        }
                    } else {
                        System.out.println("  No prerequisites for this subject");
                    }

                    if (prereqsMet) {
                        SubjectRecommendation subject = new SubjectRecommendation(courseCode, title, units, "To Take");
                        availableSubjects.add(subject);
                        System.out.println("  Subject eligible: " + courseCode);
                    } else {
                        System.out.println("  Subject not eligible due to prerequisites: " + courseCode);
                    }
                }

                System.out.println("Total subjects found in query: " + subjectCount);
                System.out.println("Eligible subjects: " + availableSubjects.size());
            }

            // Add failed subjects first if any
            if (!failedSubjects.isEmpty()) {
                System.out.println("Adding " + failedSubjects.size() + " failed subjects to take");
                recommendedSubjects.addAll(failedSubjects);
            } else {
                // If no failed subjects, add all eligible subjects
                System.out.println("No failed subjects, adding " + availableSubjects.size() + " eligible subjects");
                for (SubjectRecommendation subject : availableSubjects) {
                    recommendedSubjects.add(subject);
                    totalUnits += subject.getTotalUnits();
                }
            }

            // Update UI
            subsToTakeTableView.setItems(recommendedSubjects);
            unitsValidationTextField.setText(String.valueOf(totalUnits));

            System.out.println("Final recommended subjects: " + recommendedSubjects.size());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error loading recommended subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // This method is no longer needed as the prerequisite logic is now handled in loadRecommendedSubjects
    private Map<String, SubjectDetails> loadAllSubjects(String program) {
        Map<String, SubjectDetails> subjects = new HashMap<>();

        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SMFCOURSECODE, SMFDESCTITLE, SMFTOTALUNITS, SMFPREREQUISITES FROM SubjectManagerFile WHERE SMFCOURSE = ?");
            stmt.setString(1, program);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("SMFCOURSECODE").trim();
                String title = rs.getString("SMFDESCTITLE").trim();
                int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));
                String prereqs = rs.getString("SMFPREREQUISITES").trim();

                subjects.put(courseCode, new SubjectDetails(courseCode, title, units, prereqs));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    // Helper class to store subject details
    private static class SubjectDetails {
        private final String courseCode;
        private final String title;
        private final int units;
        private final String prerequisites;

        public SubjectDetails(String courseCode, String title, int units, String prerequisites) {
            this.courseCode = courseCode;
            this.title = title;
            this.units = units;
            this.prerequisites = prerequisites;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getTitle() {
            return title;
        }

        public int getUnits() {
            return units;
        }

        public String getPrerequisites() {
            return prerequisites;
        }
    }

    private Map<String, Double> loadStudentGrades() {
        Map<String, Double> grades = new HashMap<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SGFCOURSECODE, SGFGRADE FROM StudentGradesFile WHERE SGFSTUDENTID = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Loading grades for student: " + username);
            while (rs.next()) {
                String courseCode = rs.getString("SGFCOURSECODE").trim();
                String gradeStr = rs.getString("SGFGRADE").trim();

                System.out.println("Found grade record: " + courseCode + " = " + gradeStr);

                double grade = 5.0; // Default to failing grade
                try {
                    grade = Double.parseDouble(gradeStr);
                } catch (NumberFormatException e) {
                    System.out.println("Non-numeric grade for " + courseCode + ": " + gradeStr);
                    // Keep default 5.0 for non-numeric grades
                }

                grades.put(courseCode, grade);
            }

            System.out.println("Total grades loaded: " + grades.size());
        } catch (SQLException e) {
            System.out.println("Error loading student grades: " + e.getMessage());
            e.printStackTrace();
        }

        return grades;
    }

    private void setupButtonHandlers() {
        logoutButton.setOnAction(this::handleLogout);
        enrollButton.setOnAction(this::handleEnroll);
        evaluationButton.setDisable(true);
    }

    @FXML
    void handleEnroll(ActionEvent event) {
        ObservableList<SubjectRecommendation> selectedSubjects = subsToTakeTableView.getItems();

        if (selectedSubjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Subjects", "There are no subjects to enroll.");
            return;
        }

        try {
            Connection conn = dbConnection.getConnection();

            // First check if the student has already enrolled in any subjects
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM EnrolledSubjectsFile WHERE ESFSTUDID = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Clear previous enrollments for this student
                PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM EnrolledSubjectsFile WHERE ESFSTUDID = ?");
                deleteStmt.setString(1, username);
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }
            rs.close();
            checkStmt.close();

            // Insert new enrollments
            String insertQuery = "INSERT INTO EnrolledSubjectsFile (ESFSTUDID, ESFCOURSECODE, ESFDESCRIPTITLE, ESFTOTALUNITS, ESFSTATUS) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(insertQuery);

            int successCount = 0;

            for (SubjectRecommendation subject : selectedSubjects) {
                stmt.setString(1, username);
                stmt.setString(2, subject.getCourseCode());
                stmt.setString(3, subject.getDescriptiveTitle());
                stmt.setString(4, String.valueOf(subject.getTotalUnits()));
                stmt.setString(5, subject.getStatus());

                successCount += stmt.executeUpdate();
            }

            stmt.close();
            conn.close();

            showAlert(Alert.AlertType.INFORMATION, "Enrollment Successful",
                    "Successfully enrolled in " + successCount + " subjects.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Enrollment Error",
                    "Failed to enroll in subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            // Navigate to login screen
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate to login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}