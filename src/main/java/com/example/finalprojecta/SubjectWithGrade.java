package com.example.finalprojecta;

 // Represents a subject with a grade
 // This class extends the Subject class and adds a grade field

public class SubjectWithGrade extends Subject {
    private String grade;

    public SubjectWithGrade(String courseCode, String descriptiveTitle, int totalUnits,
                            String prerequisites, String grade) {
        super(courseCode, descriptiveTitle, totalUnits, prerequisites);
        this.grade = grade;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}