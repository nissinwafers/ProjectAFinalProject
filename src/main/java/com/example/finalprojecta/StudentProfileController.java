package com.example.finalprojecta;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import databasemodel.DatabaseConnector;

public class StudentProfileController {

    @FXML
    private TextField cityAddressText;

    @FXML
    private ComboBox<String> civilStatusComboBox;

    @FXML
    private TextField courseTextField;

    @FXML
    private TextField departmentTextField;

    @FXML
    private TextField emailAddressTextField;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField middleInitialTextField;

    @FXML
    private TextField nationalityTextField;

    @FXML
    private TextField phoneNumText;

    @FXML
    private TextField provincialAddressText;

    @FXML
    private Button saveButton;

    @FXML
    private ComboBox<String> sexComboBox;

    @FXML
    private TextField telNumText;

    // Add the evaluation button reference
    @FXML
    private Button evaluationButton;

    // Reference to the main application
    private MainProject main;

    // Username of the logged-in student
    private String username;

    // Setter for main reference
    public void setLoginMain(MainProject main) {
        this.main = main;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
        loadStudentData();
    }

    @FXML
    public void handleLogin(ActionEvent event) {

    }

    @FXML
    public void initialize() {
        sexComboBox.getItems().addAll("Male", "Female", "Other");
        civilStatusComboBox.getItems().addAll("Single", "Married", "Widowed", "Divorced");
    }

     // Handle the evaluation button click
     // Opens the student-evaluation.fxml form
    @FXML
    private void handleEvaluationButton(ActionEvent event) {
        try {
            main.showStudentEvaluation(username);  // Pass the saved username forward
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     // Loads student data from the database if it exists
    private void loadStudentData() {
        if (username == null || username.isEmpty()) {
            return;
        }

        String sql = "SELECT * FROM StudentsInfoFile WHERE STUDENTID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                firstNameTextField.setText(resultSet.getString("FIRSTNAME"));
                middleInitialTextField.setText(resultSet.getString("MIDDLENAME"));
                lastNameTextField.setText(resultSet.getString("LASTNAME"));
                nationalityTextField.setText(resultSet.getString("NATIONALITY"));
                emailAddressTextField.setText(resultSet.getString("EMAIL"));
                phoneNumText.setText(resultSet.getString("PHONENUM"));
                telNumText.setText(resultSet.getString("TELNUM"));
                cityAddressText.setText(resultSet.getString("CITYADDRESS"));
                provincialAddressText.setText(resultSet.getString("PROVINCIALADDRESS"));
                courseTextField.setText(resultSet.getString("COURSEPROGRAM"));
                departmentTextField.setText(resultSet.getString("DEPARTMENT"));

                sexComboBox.setValue(resultSet.getString("SEX"));

                setFieldsEditable(false);
                saveButton.setDisable(true);

                System.out.println("Student data loaded for ID: " + username);
            } else {
                System.out.println("No existing data found for student ID: " + username);
                setFieldsEditable(true);
                saveButton.setDisable(false);
            }

        } catch (SQLException e) {
            System.out.println("Error loading student data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveButton(ActionEvent event) throws IOException {
        if (!validateFields()) {
            return;
        }

        if (saveToDatabase()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile information saved successfully!");
            alert.showAndWait();

            setFieldsEditable(false);
            saveButton.setDisable(true);
        } else {
            showError("Database Error", "Failed to save profile information.");
        }
    }

     // Sets the editability of all input fields
    private void setFieldsEditable(boolean editable) {
        firstNameTextField.setEditable(editable);
        middleInitialTextField.setEditable(editable);
        lastNameTextField.setEditable(editable);
        nationalityTextField.setEditable(editable);
        emailAddressTextField.setEditable(editable);
        phoneNumText.setEditable(editable);
        telNumText.setEditable(editable);
        cityAddressText.setEditable(editable);
        provincialAddressText.setEditable(editable);
        courseTextField.setEditable(editable);
        departmentTextField.setEditable(editable);

        sexComboBox.setDisable(!editable);
        civilStatusComboBox.setDisable(!editable);
    }

    private boolean saveToDatabase() {
        boolean recordExists = checkIfRecordExists();

        String sql;
        if (recordExists) {
            sql = "UPDATE StudentsInfoFile SET " +
                    "FIRSTNAME = ?, MIDDLENAME = ?, LASTNAME = ?, SEX = ?, NATIONALITY = ?, " +
                    "EMAIL = ?, PHONENUM = ?, TELNUM = ?, CITYADDRESS = ?, PROVINCIALADDRESS = ?, " +
                    "COURSEPROGRAM = ?, DEPARTMENT = ? " +
                    "WHERE STUDENTID = ?";
        } else {
            sql = "INSERT INTO StudentsInfoFile " +
                    "(FIRSTNAME, MIDDLENAME, LASTNAME, SEX, NATIONALITY, " +
                    "EMAIL, PHONENUM, TELNUM, CITYADDRESS, PROVINCIALADDRESS, COURSEPROGRAM, " +
                    "DEPARTMENT, STUDENTID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstNameTextField.getText());
            stmt.setString(2, middleInitialTextField.getText());
            stmt.setString(3, lastNameTextField.getText());
            stmt.setString(4, sexComboBox.getValue());
            stmt.setString(5, nationalityTextField.getText());
            stmt.setString(6, emailAddressTextField.getText());
            stmt.setString(7, phoneNumText.getText());
            stmt.setString(8, telNumText.getText());
            stmt.setString(9, cityAddressText.getText());
            stmt.setString(10, provincialAddressText.getText());
            stmt.setString(11, courseTextField.getText());
            stmt.setString(12, departmentTextField.getText());
            stmt.setString(13, username);

            System.out.println("Executing SQL with username: " + username);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Database save error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

     // Checks if a record already exists for this student ID
    private boolean checkIfRecordExists() {
        String sql = "SELECT COUNT(*) FROM StudentsInfoFile WHERE STUDENTID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error checking if record exists: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean validateFields() {
        if (firstNameTextField.getText().isEmpty() ||
                lastNameTextField.getText().isEmpty() ||
                middleInitialTextField.getText().isEmpty() ||
                nationalityTextField.getText().isEmpty() ||
                emailAddressTextField.getText().isEmpty() ||
                phoneNumText.getText().isEmpty() ||
                telNumText.getText().isEmpty() ||
                cityAddressText.getText().isEmpty() ||
                provincialAddressText.getText().isEmpty() ||
                courseTextField.getText().isEmpty() ||
                departmentTextField.getText().isEmpty() ||
                sexComboBox.getValue() == null ||
                civilStatusComboBox.getValue() == null) {

            showError("Missing Fields", "Please fill in all fields.");
            return false;
        }

        // Validate text fields that should not contain numbers
        if (!isValidName(firstNameTextField.getText())) {
            showError("Invalid Input", "First name should not contain numbers.");
            return false;
        }

        if (!isValidName(lastNameTextField.getText())) {
            showError("Invalid Input", "Last name should not contain numbers.");
            return false;
        }

        if (!isValidName(middleInitialTextField.getText())) {
            showError("Invalid Input", "Middle initial should not contain numbers.");
            return false;
        }

        if (!isValidName(nationalityTextField.getText())) {
            showError("Invalid Input", "Nationality should not contain numbers.");
            return false;
        }

        if (!isValidName(courseTextField.getText())) {
            showError("Invalid Input", "Course should not contain numbers.");
            return false;
        }

        if (!isValidName(departmentTextField.getText())) {
            showError("Invalid Input", "Department should not contain numbers.");
            return false;
        }

        if (!isValidEmail(emailAddressTextField.getText())) {
            showError("Invalid Email", "Please enter a valid email address.");
            return false;
        }

        if (!isValidPhoneNumber(phoneNumText.getText())) {
            showError("Invalid Phone Number", "Phone number should only contain 1-11 digits.");
            return false;
        }

        if (!isValidPhoneNumber(telNumText.getText())) {
            showError("Invalid Telephone Number", "Telephone number should only contain 1-11 digits.");
            return false;
        }

        return true;
    }

    private boolean isValidName(String text) {
        return text.matches("^[A-Za-z\\s.'-]+$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhoneNumber(String number) {
        return number.matches("^\\d{1,11}$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            main.showLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}