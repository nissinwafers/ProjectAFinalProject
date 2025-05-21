package com.example.finalprojecta;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainProject extends Application {

    private static Stage primaryStage;
    private String loggedInTeacherName;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLogin();
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public void showLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        LoginController loginController = fxmlLoader.getController();
        loginController.setLoginMain(this);
        primaryStage.setScene(scene);
    }

    public void showRegister() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        RegisterController registerController = fxmlLoader.getController();
        registerController.setRegisterMain(this);
        primaryStage.setScene(scene);
    }

    // Updated method to include username parameter
    public void showStudentProfile(String username) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("student-profile.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        StudentProfileController controller = fxmlLoader.getController();
        controller.setLoginMain(this);
        controller.setUsername(username);  // Pass the username to the controller
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Profile");
    }

    // Show the student evaluation screen
    public void showStudentEvaluation(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("student-evaluation.fxml"));
        Parent root = loader.load();
        StudentEvaluationController controller = loader.getController();
        controller.setLoginMain(this);
        controller.setUsername(username);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Student Evaluation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showAdminDashboard() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("admin-subjmanager.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        AdSubjectManagerController controller = fxmlLoader.getController();
        controller.setSubjectMain(this);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Dashboard");
    }


    public void showTeacherDashboard(String teacherUsername) throws IOException {
        // Store the teacher's username
        this.loggedInTeacherName = teacherUsername;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("teacher-grade.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        TeacherGradeController controller = fxmlLoader.getController();
        controller.setTeacherMain(this);
        controller.setCurrentTeacher(teacherUsername);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Teacher Dashboard");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}