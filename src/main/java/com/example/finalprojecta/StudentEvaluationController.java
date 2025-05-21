package com.example.finalprojecta;

import databasemodel.DatabaseConnector;
import databasemodel.StudentSQL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StudentEvaluationController implements Initializable {

    @FXML
    private Button checkGradesButton;

    @FXML
    private Button proceedButton;

    @FXML
    private Button evaluationButton;

    @FXML
    private Button logoutButton;

    @FXML
    private TableColumn<SubjectWithGrade, String> p_courseCodeColumn;

    @FXML
    private TableColumn<SubjectWithGrade, String> p_descriptiveTitleColumn;

    @FXML
    private TableColumn<SubjectWithGrade, String> p_gradesColumn;

    @FXML
    private TableColumn<SubjectWithGrade, String> p_prerequisitesColumn;

    @FXML
    private TableColumn<SubjectWithGrade, Integer> p_totalUnitsColumn;

    @FXML
    private TableView<SubjectWithGrade> prevSemesterTableView;

    @FXML
    private Button profileButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button enterButton;

    @FXML
    private ComboBox<String> semesterComboBox;

    @FXML
    private ComboBox<String> yearComboBox;

    @FXML
    private Label studentNameLabel;

    private StudentModel currentStudent;
    private String username;
    private String program;
    private String year;
    private String semester;
    private DatabaseConnector dbConnection;

    public void setUsername(String username) {
        this.username = username;

        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COURSEPROGRAM, PROGRAMYEAR, SEMESTER FROM StudentsInfoFile WHERE STUDENTID = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                program = rs.getString("COURSEPROGRAM");
                year = rs.getString("PROGRAMYEAR");
                semester = rs.getString("SEMESTER");

                System.out.println("Loaded student info: " + program + " | " + year + " | " + semester);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setLoginMain(MainProject mainProject) {
        // This method can be used to set the main application instance if needed
    }

    private ObservableList<SubjectWithGrade> previousSubjects = FXCollections.observableArrayList();
    private ObservableList<Subject> recommendedSubjects = FXCollections.observableArrayList();
    private List<Subject> availableSubjects = new ArrayList<>();
    private List<String> failedSubjects = new ArrayList<>();
    private List<String> passedSubjects = new ArrayList<>();

     // Initializes the controller.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            dbConnection = new DatabaseConnector();
            try (Connection testConn = dbConnection.getConnection()) {
                if (testConn == null) {
                    System.out.println("WARNING: Database connection is null during initialization");
                } else {
                    System.out.println("Database connection successfully tested during initialization");
                }
            } catch (SQLException e) {
                System.out.println("ERROR: Failed to connect to database during initialization: " + e.getMessage());
                e.printStackTrace();
            }

            p_courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
            p_descriptiveTitleColumn.setCellValueFactory(new PropertyValueFactory<>("descriptiveTitle"));
            p_totalUnitsColumn.setCellValueFactory(new PropertyValueFactory<>("totalUnits"));
            p_prerequisitesColumn.setCellValueFactory(new PropertyValueFactory<>("prerequisites"));
            p_gradesColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));

            setupComboBoxes();

            setupButtonHandlers();

            enterButton.setOnAction(this::handleEnterButton);
        } catch (Exception e) {
            System.out.println("ERROR: Exception during controller initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupComboBoxes() {

        List<String> years = new ArrayList<>();
        years.add("1");
        years.add("2");
        years.add("3");
        years.add("4");
        yearComboBox.setItems(FXCollections.observableArrayList(years));

        List<String> semesters = new ArrayList<>();
        semesters.add("1st Semester");
        semesters.add("2nd Semester");
        semesterComboBox.setItems(FXCollections.observableArrayList(semesters));

        if (!years.isEmpty()) {
            yearComboBox.setValue(years.get(0));
        }
        if (!semesters.isEmpty()) {
            semesterComboBox.setValue(semesters.get(0));
        }
    }

     // Sets up handlers for navigation buttons.
    private void setupButtonHandlers() {

        evaluationButton.setDisable(true);
        logoutButton.setOnAction(this::handleLogout);
        checkGradesButton.setOnAction(this::handleCheckGradesButton);
    }

    /** Handles the enter button action. Loads previous subjects based on selected year and semester.*/
    @FXML
    void handleEnterButton(ActionEvent event) {
        String selectedYear = yearComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();

        if (selectedYear == null || selectedSemester == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select both a year and semester.");
            return;
        }

        String previousSemester;
        String previousYear = selectedYear;

        if (selectedSemester.equals("2nd Semester")) {
            previousSemester = "1st Semester";
        } else {
            previousSemester = "2nd Semester";
            int yearInt = Integer.parseInt(selectedYear);
            if (yearInt > 1) {
                previousYear = String.valueOf(yearInt - 1);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Previous Semester",
                        "Students in first year, first semester don't have previous semester subjects.");
                previousSubjects.clear();
                prevSemesterTableView.setItems(previousSubjects);
                return;
            }
        }

        loadPreviousSemesterSubjects(previousYear, previousSemester);
    }

    /** Load subjects from the previous semester */
    private void loadPreviousSemesterSubjects(String year, String semester) {
        previousSubjects.clear();

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                showAlert(Alert.AlertType.ERROR, "Connection Error",
                        "Could not establish database connection. Please check your database settings.");
                return;
            }

            System.out.println("Database connection established successfully");
            System.out.println("Loading subjects for Year " + year + ", " + semester);

            try {
                loadSubjectsWithQuery(conn, "SELECT * FROM SubjectManagerFile WHERE SMFPROGYEAR = ? AND SMFSEMESTER = ?",
                        year, semester);
            } catch (SQLException e1) {
                System.out.println("First query attempt failed: " + e1.getMessage());
            }

            prevSemesterTableView.setItems(previousSubjects);

            if (previousSubjects.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Subjects Found",
                        "No subjects found for " + semester + " of Year " + year);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error loading subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Helper method to load subjects using a specific query*/
    private void loadSubjectsWithQuery(Connection conn, String query, String year, String semester)
            throws SQLException {
        System.out.println("Executing query: " + query);
        System.out.println("Parameters: Year=" + year + ", Semester=" + semester);

        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, year);
        statement.setString(2, semester);

        ResultSet resultSet = statement.executeQuery();

        int count = 0;
        while (resultSet.next()) {
            count++;

            String courseCode = getStringValueSafely(resultSet, "SMFCOURSECODE", "COURSECODE");
            String descriptiveTitle = getStringValueSafely(resultSet, "SMFDESCTITLE", "DESCRIPTIVETITLE");
            String totalUnitsStr = getStringValueSafely(resultSet, "SMFTOTALUNITS", "TOTALUNITS");
            String prerequisites = getStringValueSafely(resultSet, "SMFPREREQUISITES", "PREREQUISITES");

            int totalUnits = 0;
            try {
                totalUnits = Integer.parseInt(totalUnitsStr);
            } catch (NumberFormatException e) {
                System.out.println("Warning: Could not parse total units: " + totalUnitsStr);
            }

            System.out.println("Found subject: " + courseCode + " - " + descriptiveTitle);

            String grade = getSubmittedGradeFromDB(username, courseCode);

            SubjectWithGrade subject = new SubjectWithGrade(
                    courseCode,
                    descriptiveTitle,
                    totalUnits,
                    prerequisites,
                    grade
            );


            previousSubjects.add(subject);
        }

        System.out.println("Total subjects found: " + count);
    }

    private String getSubmittedGradeFromDB(String studentId, String courseCode) {
        String grade = "";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            String query = "SELECT SFGGRADE FROM StudentsGradeFile WHERE SFGSTUDENTID = ? AND SFGCOURSECODE = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, courseCode);
            rs = stmt.executeQuery();

            if (rs.next()) {
                grade = rs.getString("SFGGRADE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return grade;
    }


    /** get a string value from ResultSet trying multiple column names*/
    private String getStringValueSafely(ResultSet rs, String... columnNames) {
        for (String columnName : columnNames) {
            try {
                return rs.getString(columnName);
            } catch (SQLException e) {

            }
        }
        return "";
    }

    @FXML
    void handleCheckGradesButton(ActionEvent event) {
        if (previousSubjects.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data", "There are no subjects to save.");
            return;
        }

        if (username == null || username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Username", "Username is not set. Cannot save data.");
            return;
        }

        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
                return;
            }

            String insertSQL = "INSERT INTO PreviousSubjectFile " +
                    "(PSFUSERNAME, PSFCOURSECODE, PSFDESCTITLE, PSFTOTALUNITS, PSFPREREQUISITES, PSFPROGRAMYEAR, PSFSEMESTER) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            Integer selectedYear = Integer.valueOf(yearComboBox.getValue());
            String selectedSemester = semesterComboBox.getValue();

            statement = conn.prepareStatement(insertSQL);


            for (SubjectWithGrade subject : previousSubjects) {
                statement.setString(1, username);  // student username
                statement.setString(2, subject.getCourseCode());
                statement.setString(3, subject.getDescriptiveTitle());
                statement.setString(4, String.valueOf(subject.getTotalUnits()));
                statement.setString(5, subject.getPrerequisites());
                statement.setString(6, String.valueOf(selectedYear));
                statement.setString(7, selectedSemester);
                statement.addBatch();
            }

            int[] result = statement.executeBatch();
            showAlert(Alert.AlertType.INFORMATION, "Success", result.length + " subject(s) saved to the database.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving subjects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Handles the logout button action.*/
    @FXML
    void handleLogout(ActionEvent event) {
        try {
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

    @FXML
    private void handleProceedButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("student-evaluation2.fxml"));
            Parent root = loader.load();

            StudentEvalTwoController controller = loader.getController();
            controller.setStudentInfo(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Evaluation - Recommended Subjects");
            stage.show();

        } catch (IOException e) {
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