package com.campus_trade.dto;

public class VerifyRequest {

    private String schoolName;

    private String studentId;

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    @Override
    public String toString() {
        return "VerifyRequest{schoolName='" + schoolName + "', studentId='" + studentId + "'}";
    }
}
