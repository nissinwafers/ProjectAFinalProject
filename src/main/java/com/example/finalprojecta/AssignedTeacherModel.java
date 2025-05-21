package com.example.finalprojecta;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AssignedTeacherModel {
    private final StringProperty courseCode;
    private final StringProperty descriptiveTitle;
    private final StringProperty professor;

    public AssignedTeacherModel(String courseCode, String descriptiveTitle, String professor) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.descriptiveTitle = new SimpleStringProperty(descriptiveTitle);
        this.professor = new SimpleStringProperty(professor);
    }

    // Getters and setters
    public String getCourseCode() {
        return courseCode.get();
    }

    public void setCourseCode(String value) {
        courseCode.set(value);
    }

    public StringProperty courseCodeProperty() {
        return courseCode;
    }

    public String getDescriptiveTitle() {
        return descriptiveTitle.get();
    }

    public void setDescriptiveTitle(String value) {
        descriptiveTitle.set(value);
    }

    public StringProperty descriptiveTitleProperty() {
        return descriptiveTitle;
    }

    public String getProfessor() {
        return professor.get();
    }

    public void setProfessor(String value) {
        professor.set(value);
    }

    public StringProperty professorProperty() {
        return professor;
    }
}