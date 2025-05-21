package databasemodel;

import com.example.finalprojecta.StudentModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class StudentSQL {

    /**
     * Get all students from the database
     * @return ObservableList of StudentModel objects
     */
    public static ObservableList<StudentModel> getAllStudents() {
        ObservableList<StudentModel> studentList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM StudentsInfoFile";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Create a student model with all fields from the database
                StudentModel student = createStudentFromResultSet(rs);
                studentList.add(student);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
        }

        return studentList;
    }

    /**
     * Get students filtered by program
     * @param program The program to filter by
     * @return ObservableList of StudentModel objects
     */
    public static ObservableList<StudentModel> getStudentsByProgram(String program) {
        ObservableList<StudentModel> studentList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM StudentsInfoFile WHERE COURSEPROGRAM = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, program);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentModel student = createStudentFromResultSet(rs);
                studentList.add(student);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching students by program: " + e.getMessage());
            e.printStackTrace();
        }

        return studentList;
    }

    /**
     * Get students filtered by year
     * @param year The year to filter by
     * @return ObservableList of StudentModel objects
     */
    public static ObservableList<StudentModel> getStudentsByYear(int year) {
        ObservableList<StudentModel> studentList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM StudentsInfoFile WHERE PROGRAMYEAR = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentModel student = createStudentFromResultSet(rs);
                studentList.add(student);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching students by year: " + e.getMessage());
            e.printStackTrace();
        }

        return studentList;
    }

    /**
     * Get students filtered by semester
     * @param semester The semester to filter by
     * @return ObservableList of StudentModel objects
     */
    public static ObservableList<StudentModel> getStudentsBySemester(String semester) {
        ObservableList<StudentModel> studentList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM StudentsInfoFile WHERE SEMESTER = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, semester);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentModel student = createStudentFromResultSet(rs);
                studentList.add(student);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching students by semester: " + e.getMessage());
            e.printStackTrace();
        }

        return studentList;
    }

    /**
     * Get students filtered by multiple criteria
     * @param program The program to filter by (or null for all programs)
     * @param year The year to filter by (or 0 for all years)
     * @param semester The semester to filter by (or null for all semesters)
     * @return ObservableList of StudentModel objects
     */
    public static ObservableList<StudentModel> getFilteredStudents(String program, Integer year, String semester) {
        ObservableList<StudentModel> studentList = FXCollections.observableArrayList();

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM StudentsInfoFile WHERE 1=1");
        boolean hasProgram = program != null && !program.equals("All Programs");
        boolean hasYear = year != null && year > 0;
        boolean hasSemester = semester != null && !semester.equals("All Semesters");

        if (hasProgram) {
            sqlBuilder.append(" AND COURSEPROGRAM = ?");
        }
        if (hasYear) {
            sqlBuilder.append(" AND PROGRAMYEAR = ?");
        }
        if (hasSemester) {
            sqlBuilder.append(" AND SEMESTER = ?");
        }

        String sql = sqlBuilder.toString();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (hasProgram) {
                stmt.setString(paramIndex++, program);
            }
            if (hasYear) {
                stmt.setInt(paramIndex++, year);
            }
            if (hasSemester) {
                stmt.setString(paramIndex, semester);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentModel student = createStudentFromResultSet(rs);
                studentList.add(student);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching filtered students: " + e.getMessage());
            e.printStackTrace();
        }

        return studentList;
    }

    /**
     * Get all unique programs from the database
     * @return ObservableList of program names
     */
    public static ObservableList<String> getAllPrograms() {
        ObservableList<String> programList = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT COURSEPROGRAM FROM StudentsInfoFile ORDER BY COURSEPROGRAM";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String program = rs.getString("COURSEPROGRAM");
                if (program != null && !program.isEmpty()) {
                    programList.add(program);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching programs: " + e.getMessage());
            e.printStackTrace();
        }

        return programList;
    }

    /**
     * Get all unique years from the database
     * @return ObservableList of years
     */
    public static ObservableList<Integer> getAllYears() {
        ObservableList<Integer> yearList = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT PROGRAMYEAR FROM StudentsInfoFile ORDER BY PROGRAMYEAR";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int year = rs.getInt("PROGRAMYEAR");
                if (!rs.wasNull()) {
                    yearList.add(year);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching years: " + e.getMessage());
            e.printStackTrace();
        }

        return yearList;
    }

    /**
     * Get all unique semesters from the database
     * @return ObservableList of semester names
     */
    public static ObservableList<String> getAllSemesters() {
        ObservableList<String> semesterList = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT SEMESTER FROM StudentsInfoFile ORDER BY SEMESTER";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String semester = rs.getString("SEMESTER");
                if (semester != null && !semester.isEmpty()) {
                    semesterList.add(semester);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching semesters: " + e.getMessage());
            e.printStackTrace();
        }

        return semesterList;
    }

    /**
     * Helper method to create a StudentModel from a ResultSet
     * @param rs The ResultSet containing student data
     * @return A StudentModel object
     * @throws SQLException If there's an error reading from the ResultSet
     */
    private static StudentModel createStudentFromResultSet(ResultSet rs) throws SQLException {
        // Extract the middle initial from the middle name
        String middleName = rs.getString("MIDDLENAME");
        String middleInitial = "";
        if (middleName != null && !middleName.isEmpty()) {
            middleInitial = middleName.substring(0, 1) + ".";
        }

        return new StudentModel(
                // Use the middle initial for display in table
        );
    }

    public static StudentModel getStudentByStudentId(String studentId) {
        String sql = "SELECT * FROM StudentsInfoFile WHERE STUDENTID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(studentId)); // because STUDENTID is a Number
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StudentModel student = new StudentModel();
                student.setStudentId(String.valueOf(rs.getInt("STUDENTID")));
                student.setFirstName(rs.getString("FIRSTNAME"));
                student.setLastName(rs.getString("LASTNAME"));
                return student;
            }

        } catch (SQLException e) {
            System.out.println("Student lookup error: " + e.getMessage());
        }

        return null;
    }

}