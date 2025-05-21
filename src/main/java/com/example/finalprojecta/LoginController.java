package com.example.finalprojecta;

import databasemodel.UserSQL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    private String username;

    @FXML
    private TextField login_idnumber;

    @FXML
    private Button login_loginButton;

    @FXML
    private TextField login_passvisible;

    @FXML
    private PasswordField login_password;

    @FXML
    private CheckBox login_showPassword;

    @FXML
    private Button login_signupButton;

    // Reference sa MainSystem para maka-navigate ta sa next screens
    private MainProject loginMain;

    // Setter para ma-set ang reference sa MainSystem
    public void setLoginMain(MainProject loginMain) {
        this.loginMain = loginMain;
    }

    @FXML
    private void handleLogin() {
        String username = login_idnumber.getText();
        String password;

        if (login_showPassword.isSelected()) {
            password = login_passvisible.getText();
        } else {
            password = login_password.getText();
        }

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields.");
            alert.showAndWait();
            return;
        }

        String role = UserSQL.login(username, password);
        if (role != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Successful");
            alert.setHeaderText(null);
            alert.setContentText("Welcome, " + role);
            alert.showAndWait();

            try {
                navigateByRole(role, username);
            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Navigation Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Error navigating to the appropriate screen: " + e.getMessage());
                errorAlert.showAndWait();
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid credentials.");
            alert.showAndWait();
        }
    }

    // handle navigation based on role and pass username
    private void navigateByRole(String role, String username) throws IOException {
        switch (role.toLowerCase()) {
            case "student":
                loginMain.showStudentProfile(username);
                break;
            case "admin":
                loginMain.showAdminDashboard();
                break;
            case "teacher":
                loginMain.showTeacherDashboard(username);
                break;
            default:
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Role Error");
                alert.setHeaderText(null);
                alert.setContentText("Unknown role: " + role);
                alert.showAndWait();
                break;
        }
    }

    //Visibility sa password
    @FXML
    private void handlePasswordVisibility() {
        if (login_showPassword.isSelected()) {
            login_passvisible.setText(login_password.getText());
            login_passvisible.setVisible(true);
            login_passvisible.setManaged(true);

            login_password.setVisible(false);
            login_password.setManaged(false);
        } else {
            login_password.setText(login_passvisible.getText());
            login_password.setVisible(true);
            login_password.setManaged(true);

            login_passvisible.setVisible(false);
            login_passvisible.setManaged(false);
        }
    }

    @FXML
    private void handleSignupButton() {
        try {
            loginMain.showRegister();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}