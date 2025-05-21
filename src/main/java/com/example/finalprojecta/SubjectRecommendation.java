package com.example.finalprojecta;

 // Class representing a subject recommendation.
 // Used for displaying recommended subjects in the student evaluation UI.
public class SubjectRecommendation {
    private String courseCode;
    private String descriptiveTitle;
    private int totalUnits;
    private String status;

    public SubjectRecommendation(String courseCode, String descriptiveTitle, int totalUnits, String status) {
        this.courseCode = courseCode;
        this.descriptiveTitle = descriptiveTitle;
        this.totalUnits = totalUnits;
        this.status = status;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescriptiveTitle() {
        return descriptiveTitle;
    }

    public void setDescriptiveTitle(String descriptiveTitle) {
        this.descriptiveTitle = descriptiveTitle;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return courseCode + " - " + descriptiveTitle + " (" + status + ")";
    }
}