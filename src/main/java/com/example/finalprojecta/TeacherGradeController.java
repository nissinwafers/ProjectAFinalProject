package com.example.finalprojecta;

import databasemodel.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class TeacherGradeController implements Initializable {

    @FXML
    private ComboBox<String> courseCodeComboBox;

    @FXML
    private TableColumn<TStudentGrade, Double> gradeColumn;

    @FXML
    private TableView<TStudentGrade> gradesInputTableView;

    @FXML
    private TableColumn<TStudentGrade, String> idNumberColumn;

    @FXML
    private TableColumn<TStudentGrade, String> courseCodeColumn;

    @FXML
    private Button logoutButton;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Button submitGradesButton;

    // Reference to the main application
    private MainProject main;

    // Store current logged in teacher's name
    private String currentTeacherName;

    // Observable list for the table view
    private ObservableList<TStudentGrade> studentGradesList = FXCollections.observableArrayList();

    public void setTeacherMain(MainProject main) {
        this.main = main;
    }

    public void setCurrentTeacher(String teacherName) {
        this.currentTeacherName = teacherName;
        System.out.println("Logged-in teacher (Java): '" + currentTeacherName + "'");

        // Only load courses if the controller is already initialized
        if (courseCodeComboBox != null) {
            loadAssignedCourses();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gradesInputTableView.setItems(FXCollections.observableArrayList(
                new TStudentGrade("12345", "CS101", 95.0)
        ));

        idNumberColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));

        gradesInputTableView.setEditable(true);
        gradeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        gradeColumn.setOnEditCommit(event -> {
            TStudentGrade studentGrade = event.getRowValue();
            studentGrade.setGrade(event.getNewValue());
        });

        courseCodeComboBox.setOnAction(event -> {
            if (courseCodeComboBox.getValue() != null) {
                loadStudentsForCourse(courseCodeComboBox.getValue());
            }
        });

        submitGradesButton.setOnAction(event -> submitGrades());

        if (currentTeacherName != null && !currentTeacherName.isEmpty()) {
            loadAssignedCourses();
        }
    }

     // Loads all courses assigned to the current teacher
    private void loadAssignedCourses() {
        ObservableList<String> courses = FXCollections.observableArrayList();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnector.getConnection();
            String query = "SELECT ATFCOURSECODE FROM AssignedTchrsFile WHERE ATFTEACHERSNAME = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, currentTeacherName);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                courses.add(resultSet.getString("ATFCOURSECODE"));
            }

            courseCodeComboBox.setItems(courses);

            if (!courses.isEmpty()) {
                courseCodeComboBox.setValue(courses.get(0));
                loadStudentsForCourse(courses.get(0));
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load assigned courses", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnterButton() {
        if (courseCodeComboBox.getValue() != null) {
            loadStudentsForCourse(courseCodeComboBox.getValue());
        } else {
            showAlert("Warning", "No Course Selected", "Please select a course code first.");
        }
    }

     // Loads students enrolled in the selected course
    private void loadStudentsForCourse(String courseCode) {
        studentGradesList.clear();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnector.getConnection();

            String query = "SELECT DISTINCT PSFUSERNAME, PSFCOURSECODE " +
                    "FROM PreviousSubjectFile " +
                    "WHERE PSFCOURSECODE = ?";

            statement = connection.prepareStatement(query);
            statement.setString(1, courseCode);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String studentId = resultSet.getString("PSFUSERNAME");
                String matchedCourseCode = resultSet.getString("PSFCOURSECODE");

                Double existingGrade = getExistingGrade(studentId, matchedCourseCode);

                TStudentGrade studentGrade = new TStudentGrade(studentId, matchedCourseCode, existingGrade);
                studentGradesList.add(studentGrade);
            }

            gradesInputTableView.setItems(studentGradesList);

            System.out.println("Loaded students for course: " + courseCode);
            for (TStudentGrade sg : studentGradesList) {
                System.out.println(sg.getStudentId() + " | " + sg.getCourseCode());
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load students", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

     // Gets any existing grade for a student and course
    private Double getExistingGrade(String studentId, String courseCode) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnector.getConnection();
            String query = "SELECT SGFGRADE FROM StudentGradesFile WHERE SGFSTUDENTID = ? AND SGFCOURSECODE = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, studentId);
            statement.setString(2, courseCode);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String gradeStr = resultSet.getString("SGFGRADE");
                try {
                    return Double.parseDouble(gradeStr);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

     // Submits grades for all students in the current course
    private void submitGrades() {
        String courseCode = courseCodeComboBox.getValue();
        if (courseCode == null || courseCode.isEmpty()) {
            showAlert("Error", "No Course Selected", "Please select a course code first.");
            return;
        }

        Connection connection = null;
        PreparedStatement insertStatement = null;
        PreparedStatement updateStatement = null;

        try {

            connection = DatabaseConnector.getConnection();
            connection.setAutoCommit(false);

            String insertQuery = "INSERT INTO StudentsGradeFile (SFGSTUDENTID, SFGCOURSECODE, SFGGRADE) VALUES (?, ?, ?)";
            String updateQuery = "UPDATE StudentsGradeFile SET SFGGRADE = ? WHERE SFGSTUDENTID = ? AND SFGCOURSECODE = ?";

            insertStatement = connection.prepareStatement(insertQuery);
            updateStatement = connection.prepareStatement(updateQuery);

            for (TStudentGrade studentGrade : studentGradesList) {
                if (studentGrade.getGrade() == null || studentGrade.getGrade().toString().trim().isEmpty()) {
                    System.out.println("Grade is null or empty for student: " + studentGrade.getStudentId());
                    continue;
                }

                System.out.println("Attempting to save grade...");
                System.out.println("Student ID: " + studentGrade.getStudentId());
                System.out.println("Course Code: " + courseCode);
                System.out.println("Grade: " + studentGrade.getGrade());

                boolean exists = recordExists(connection, studentGrade.getStudentId(), courseCode);
                System.out.println(exists ? "Updating existing record..." : "Inserting new record...");

                if (exists) {
                    updateStatement.setString(1, String.valueOf(studentGrade.getGrade()));
                    updateStatement.setString(2, studentGrade.getStudentId());
                    updateStatement.setString(3, courseCode);
                    int rows = updateStatement.executeUpdate();
                    System.out.println("Rows affected (update): " + rows);
                } else {
                    insertStatement.setString(1, studentGrade.getStudentId());
                    insertStatement.setString(2, courseCode);
                    insertStatement.setString(3, String.valueOf(studentGrade.getGrade()));
                    int rows = insertStatement.executeUpdate();
                    System.out.println("Rows affected (insert): " + rows);
                }
            }

            connection.commit();
            showAlert("Success", "Grades Submitted", "All grades have been successfully submitted.");

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert("Database Error", "Failed to submit grades", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (insertStatement != null) {
                try {
                    insertStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (updateStatement != null) {
                try {
                    updateStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

     // Checks if a record already exists for a student and course
    private boolean recordExists(Connection connection, String studentId, String courseCode) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT COUNT(*) FROM StudentsGradeFile WHERE SFGSTUDENTID = ? AND SFGCOURSECODE = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, studentId);
            statement.setString(2, courseCode);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

            return false;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            main.showLogin();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Logout Failed", "Unable to return to login screen: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}