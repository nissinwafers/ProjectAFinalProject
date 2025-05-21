package com.example.finalprojecta;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Subject {
    private final StringProperty courseCode;
    private final StringProperty descriptiveTitle;
    private final IntegerProperty totalUnits;
    private final StringProperty prerequisites;

    public Subject(String courseCode, String descriptiveTitle, int totalUnits, String prerequisites) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.descriptiveTitle = new SimpleStringProperty(descriptiveTitle);
        this.totalUnits = new SimpleIntegerProperty(totalUnits);
        this.prerequisites = new SimpleStringProperty(prerequisites);
    }

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

    public int getTotalUnits() {
        return totalUnits.get();
    }

    public void setTotalUnits(int value) {
        totalUnits.set(value);
    }

    public IntegerProperty totalUnitsProperty() {
        return totalUnits;
    }

    public String getPrerequisites() {
        return prerequisites.get();
    }

    public void setPrerequisites(String value) {
        prerequisites.set(value);
    }

    public StringProperty prerequisitesProperty() {
        return prerequisites;
    }

    @Override
    public String toString() {
        return courseCode.get() + " - " + descriptiveTitle.get();
    }
}