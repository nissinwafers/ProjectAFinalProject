package com.example.finalprojecta;

import databasemodel.UserSQL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;


public class RegisterController {

    @FXML
    private PasswordField register_confirmm;

    @FXML
    private TextField register_confirmvisible;

    @FXML
    private TextField register_idnumber;

    @FXML
    private Button register_loginButton;

    @FXML
    private TextField register_passvisible;

    @FXML
    private PasswordField register_password;

    @FXML
    private Button register_registerButton;

    @FXML
    private ComboBox<String> register_roleComboBox;

    @FXML
    private CheckBox register_showPassword;

    // Reference sa MainSystem para maka-navigate sa next screens
    private MainProject registerMain;

    public void setRegisterMain(MainProject registerMain) {
        this.registerMain = registerMain;
    }

    @FXML
    public void handleLogin(ActionEvent event) {

    }

    @FXML
    public void initialize() {
        register_roleComboBox.getItems().addAll("Admin", "Student", "Teacher");
    }

    //password visibility
    @FXML
    private void togglePasswordVisibility() {
        boolean show = register_showPassword.isSelected();

        register_passvisible.setText(register_password.getText());
        register_passvisible.setVisible(show);
        register_passvisible.setManaged(show);
        register_password.setVisible(!show);
        register_password.setManaged(!show);

        register_confirmvisible.setText(register_confirmm.getText());
        register_confirmvisible.setVisible(show);
        register_confirmvisible.setManaged(show);
        register_confirmm.setVisible(!show);
        register_confirmm.setManaged(!show);
    }

    @FXML
    private void handleRegister() {
        String role = (String) register_roleComboBox.getValue();
        String username = register_idnumber.getText();
        String password = register_showPassword.isSelected()
                ? register_passvisible.getText()
                : register_password.getText();
        String confirmPassword = register_showPassword.isSelected()
                ? register_confirmvisible.getText()
                : register_confirmm.getText();

        if (role == null || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.WARNING, "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Passwords do not match.");
            return;
        }

        boolean success = UserSQL.registerUser(role, username, password);
        if (success) {
            showAlert(AlertType.INFORMATION, "Registered successfully!");
            clearFields();
        } else {
            showAlert(AlertType.ERROR, "Registration failed. Username might already exist.");
        }
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Registration");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        register_roleComboBox.setValue(null);
        register_idnumber.clear();
        register_password.clear();
        register_passvisible.clear();
        register_confirmm.clear();
        register_confirmvisible.clear();
        register_showPassword.setSelected(false);
        togglePasswordVisibility();
    }

    @FXML
    private void handleBackToLogin() {
        try {
            registerMain.showLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

