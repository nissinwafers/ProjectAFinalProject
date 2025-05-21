package databasemodel;

import com.example.finalprojecta.AssignedTeacherModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignedTeacherSQL {

    private Connection connection;

    public AssignedTeacherSQL() {
        try {
            // Get database connection
            connection = DatabaseConnector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    // Get all assigned teachers from the database
    public ObservableList<AssignedTeacherModel> getAllAssignedTeachers() {
        ObservableList<AssignedTeacherModel> assignedTeacherList = FXCollections.observableArrayList();

        try {
            String query = "SELECT ATFCOURSECODE, ATFDESCTITLE, ATFTEACHERSNAME FROM AssignedTchrsFile";
            PreparedStatement pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("ATFCOURSECODE");
                String descriptiveTitle = rs.getString("ATFDESCTITLE");
                String professor = rs.getString("ATFTEACHERSNAME");

                assignedTeacherList.add(new AssignedTeacherModel(courseCode, descriptiveTitle, professor));
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving assigned teachers: " + e.getMessage());
        }

        return assignedTeacherList;
    }

    // Get assigned teachers filtered by semester, year, and school year
    public ObservableList<AssignedTeacherModel> getAssignedTeachers(String semester, String year, String schoolYear) {
        ObservableList<AssignedTeacherModel> assignedTeacherList = FXCollections.observableArrayList();

        try {
            String query = "SELECT ATFCOURSECODE, ATFDESCTITLE, ATFTEACHERSNAME FROM AssignedTchrsFile " +
                    "WHERE ATFSEMESTER = ? AND ATFYEAR = ? AND ATFSCHOOLYR = ?";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, semester);
            pst.setString(2, year);
            pst.setString(3, schoolYear);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("ATFCOURSECODE");
                String descriptiveTitle = rs.getString("ATFDESCTITLE");
                String professor = rs.getString("ATFTEACHERSNAME");

                assignedTeacherList.add(new AssignedTeacherModel(courseCode, descriptiveTitle, professor));
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving filtered assigned teachers: " + e.getMessage());
        }

        return assignedTeacherList;
    }

    // Insert a new assigned teacher
    public boolean insertAssignedTeacher(String courseCode, String descriptiveTitle, String professor,
                                         String semester, String year, String schoolYear) {
        try {
            String query = "INSERT INTO AssignedTchrsFile (ATFTEACHERSNAME, ATFCOURSECODE, ATFSEMESTER, ATFYEAR, ATFSCHOOLYR, ATFDESCTITLE) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, professor);
            pst.setString(2, courseCode);
            pst.setString(3, semester);
            pst.setString(4, year);
            pst.setString(5, schoolYear);
            pst.setString(6, descriptiveTitle);

            int rowsAffected = pst.executeUpdate();
            pst.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error inserting assigned teacher: " + e.getMessage());
            return false;
        }
    }

    // Update an existing assigned teacher
    public boolean updateAssignedTeacher(String originalCourseCode, String courseCode, String descriptiveTitle,
                                         String professor, String semester, String year, String schoolYear) {
        try {
            String query = "UPDATE AssignedTchrsFile SET ATFCOURSECODE = ?, ATFDESCTITLE = ?, ATFTEACHERSNAME = ?, " +
                    "ATFSEMESTER = ?, ATFYEAR = ?, ATFSCHOOLYR = ? WHERE ATFCOURSECODE = ?";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, courseCode);
            pst.setString(2, descriptiveTitle);
            pst.setString(3, professor);
            pst.setString(4, semester);
            pst.setString(5, year);
            pst.setString(6, schoolYear);
            pst.setString(7, originalCourseCode);

            int rowsAffected = pst.executeUpdate();
            pst.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating assigned teacher: " + e.getMessage());
            return false;
        }
    }

    // Delete an assigned teacher
    public boolean deleteAssignedTeacher(String courseCode) {
        try {
            String query = "DELETE FROM AssignedTchrsFile WHERE ATFCOURSECODE = ?";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, courseCode);

            int rowsAffected = pst.executeUpdate();
            pst.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error deleting assigned teacher: " + e.getMessage());
            return false;
        }
    }

    // Delete assigned teachers by semester, year, and school year
    public boolean deleteAssignedTeachers(String semester, String year, String schoolYear) {
        try {
            String query = "DELETE FROM AssignedTchrsFile WHERE ATFSEMESTER = ? AND ATFYEAR = ? AND ATFSCHOOLYR = ?";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, semester);
            pst.setString(2, year);
            pst.setString(3, schoolYear);

            int rowsAffected = pst.executeUpdate();
            pst.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error deleting assigned teachers: " + e.getMessage());
            return false;
        }
    }

    // Save all teachers in the list to the database for specific semester, year and school year
    public boolean saveAllTeachers(ObservableList<AssignedTeacherModel> teacherList,
                                   String semester, String year, String schoolYear) {
        try {
            // First, delete existing records for this semester, year and school year
            deleteAssignedTeachers(semester, year, schoolYear);

            // Now insert all the teachers from the list
            boolean allSuccessful = true;
            for (AssignedTeacherModel teacher : teacherList) {
                boolean success = insertAssignedTeacher(
                        teacher.getCourseCode(),
                        teacher.getDescriptiveTitle(),
                        teacher.getProfessor(),
                        semester,
                        year,
                        schoolYear
                );

                if (!success) {
                    allSuccessful = false;
                }
            }

            return allSuccessful;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error saving all teachers: " + e.getMessage());
            return false;
        }
    }

    // Close database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}