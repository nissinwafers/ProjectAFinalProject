package databasemodel;

import com.example.finalprojecta.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserSQL {
    public static boolean registerUser(String role, String username, String password) {
        String sql = "INSERT INTO UsersFile (Role, Username, Password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM UsersFile WHERE Username = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Check error: " + e.getMessage());
            return false;
        }
    }

    public static String login(String username, String password) {
        String sql = "SELECT Role FROM UsersFile WHERE Username = ? AND Password = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Role");
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    public static String getFullNameByUsername(String username) {
        String fullName = null;
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT FullName FROM UsersFile WHERE Username = ?")) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                fullName = resultSet.getString("FullName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fullName;
    }

    //SUBJECT MANAGER + Get all subjects based on filters
/*
    public static List<Subject> getSubjects(String course, int programYear, String semester, String schoolYear) {
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM SubjectManagerFile WHERE COURSE = ? AND PROGRAMYEAR = ? AND SEMESTER = ? AND SCHOOLYEAR = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, course);
            pstmt.setInt(2, programYear);
            pstmt.setString(3, semester);
            pstmt.setString(4, schoolYear);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Subject subject = new Subject(
                            rs.getInt("SUBJECTID"),
                            rs.getString("COURSECODE"),
                            rs.getString("DESCRIPTIVETITLE"),
                            rs.getInt("TOTALUNITS"),
                            rs.getString("PREREQUISITES"),
                            rs.getString("COURSE"),
                            rs.getInt("PROGRAMYEAR"),
                            rs.getString("SEMESTER"),
                            rs.getString("SCHOOLYEAR")
                    );
                    subjects.add(subject);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving subjects: " + e.getMessage());
            e.printStackTrace();
        }

        return subjects;
    }

    // Add a new subject
    public static boolean addSubject(Subject subject) {
        String query = "INSERT INTO SubjectManagerFile (COURSECODE, DESCRIPTIVETITLE, TOTALUNITS, PREREQUISITES, COURSE, PROGRAMYEAR, SEMESTER, SCHOOLYEAR) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, subject.getCourseCode());
            pstmt.setString(2, subject.getDescriptiveTitle());
            pstmt.setInt(3, subject.getTotalUnits());
            pstmt.setString(4, subject.getPrerequisites());
            pstmt.setString(5, subject.getCourse());
            pstmt.setInt(6, subject.getProgramYear());
            pstmt.setString(7, subject.getSemester());
            pstmt.setString(8, subject.getSchoolYear());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error adding subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete a subject by ID
    public static boolean deleteSubject(int subjectId) {
        String query = "DELETE FROM SubjectManagerFile WHERE SUBJECTID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, subjectId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update a subject
    public static boolean updateSubject(Subject subject) {
        String query = "UPDATE SubjectManagerFile SET COURSECODE = ?, DESCRIPTIVETITLE = ?, TOTALUNITS = ?, " +
                "PREREQUISITES = ?, COURSE = ?, PROGRAMYEAR = ?, SEMESTER = ?, SCHOOLYEAR = ? " +
                "WHERE SUBJECTID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, subject.getCourseCode());
            pstmt.setString(2, subject.getDescriptiveTitle());
            pstmt.setInt(3, subject.getTotalUnits());
            pstmt.setString(4, subject.getPrerequisites());
            pstmt.setString(5, subject.getCourse());
            pstmt.setInt(6, subject.getProgramYear());
            pstmt.setString(7, subject.getSemester());
            pstmt.setString(8, subject.getSchoolYear());
            pstmt.setInt(9, subject.getSubjectId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
*/

}

