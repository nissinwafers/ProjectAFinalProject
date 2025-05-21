
package databasemodel;

import com.example.finalprojecta.SubjectWithGrade;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database handler for subject grades.
 */
public class GradesSQL {

    /**
     * Gets all subjects with grades for a student.
     * @param studentId The student ID
     * @param semester The semester
     * @param year The program year
     * @param program The course program
     * @return ObservableList of subjects with grades
     */
    public static ObservableList<SubjectWithGrade> getSubjectsWithGrades(String studentId, String semester, int year, String program) {
        ObservableList<SubjectWithGrade> subjectList = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT s.SMFCOURSECODE, s.SMFDESCTITLE, s.SMFTOTALUNITS, s.SMFPREREQUISITES, g.GRADE " +
                    "FROM SubjectManagerFile s " +
                    "LEFT JOIN StudentGrades g ON s.SMFCOURSECODE = g.COURSECODE AND g.STUDENTID = ? " +
                    "WHERE s.SMFPROGYEAR = ? AND s.SMFSEMESTER = ? AND s.SMFCOURSE = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            pstmt.setInt(2, year);
            pstmt.setString(3, semester);
            pstmt.setString(4, program);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("SMFCOURSECODE");
                String title = rs.getString("SMFDESCTITLE");
                int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));
                String prerequisites = rs.getString("SMFPREREQUISITES");
                String grade = rs.getString("GRADE");

                SubjectWithGrade subject = new SubjectWithGrade(courseCode, title, units, prerequisites, grade);
                subjectList.add(subject);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subjects with grades: " + e.getMessage());
            e.printStackTrace();
        }

        return subjectList;
    }

    /**
     * Gets all failed subjects for a student across all semesters.
     * @param studentId The student ID
     * @return ObservableList of failed subjects
     */
    public static ObservableList<SubjectWithGrade> getFailedSubjects(String studentId) {
        ObservableList<SubjectWithGrade> failedSubjects = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT s.SMFCOURSECODE, s.SMFDESCTITLE, s.SMFTOTALUNITS, s.SMFPREREQUISITES, g.GRADE " +
                    "FROM SubjectManagerFile s " +
                    "JOIN StudentGrades g ON s.SMFCOURSECODE = g.COURSECODE " +
                    "WHERE g.STUDENTID = ? AND g.GRADE > '3.0' AND g.GRADE <= '5.0'";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("SMFCOURSECODE");
                String title = rs.getString("SMFDESCTITLE");
                int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));
                String prerequisites = rs.getString("SMFPREREQUISITES");
                String grade = rs.getString("GRADE");

                SubjectWithGrade subject = new SubjectWithGrade(courseCode, title, units, prerequisites, grade);
                failedSubjects.add(subject);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching failed subjects: " + e.getMessage());
            e.printStackTrace();
        }

        return failedSubjects;
    }

    /**
     * Gets all passed subjects for a student across all semesters.
     * @param studentId The student ID
     * @return ObservableList of passed subjects
     */
    public static ObservableList<SubjectWithGrade> getPassedSubjects(String studentId) {
        ObservableList<SubjectWithGrade> passedSubjects = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT s.SMFCOURSECODE, s.SMFDESCTITLE, s.SMFTOTALUNITS, s.SMFPREREQUISITES, g.GRADE " +
                    "FROM SubjectManagerFile s " +
                    "JOIN StudentGrades g ON s.SMFCOURSECODE = g.COURSECODE " +
                    "WHERE g.STUDENTID = ? AND g.GRADE >= '1.0' AND g.GRADE <= '3.0'";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String courseCode = rs.getString("SMFCOURSECODE");
                String title = rs.getString("SMFDESCTITLE");
                int units = Integer.parseInt(rs.getString("SMFTOTALUNITS"));
                String prerequisites = rs.getString("SMFPREREQUISITES");
                String grade = rs.getString("GRADE");

                SubjectWithGrade subject = new SubjectWithGrade(courseCode, title, units, prerequisites, grade);
                passedSubjects.add(subject);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching passed subjects: " + e.getMessage());
            e.printStackTrace();
        }

        return passedSubjects;
    }

    /**
     * Saves grades for a student.
     * @param studentId The student ID
     * @param courseCode The course code
     * @param grade The grade
     * @return true if successful, false otherwise
     */
    public static boolean saveGrade(String studentId, String courseCode, String grade) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // First check if a record already exists
            String checkSql = "SELECT COUNT(*) FROM StudentGrades WHERE STUDENTID = ? AND COURSECODE = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, studentId);
            checkStmt.setString(2, courseCode);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                // Update existing record
                String updateSql = "UPDATE StudentGrades SET GRADE = ? WHERE STUDENTID = ? AND COURSECODE = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, grade);
                updateStmt.setString(2, studentId);
                updateStmt.setString(3, courseCode);

                return updateStmt.executeUpdate() > 0;
            } else {
                // Insert new record
                String insertSql = "INSERT INTO StudentGrades (STUDENTID, COURSECODE, GRADE) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, studentId);
                insertStmt.setString(2, courseCode);
                insertStmt.setString(3, grade);

                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error saving grade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

