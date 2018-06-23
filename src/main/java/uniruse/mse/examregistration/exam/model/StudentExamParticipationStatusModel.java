package uniruse.mse.examregistration.exam.model;

import uniruse.mse.examregistration.exam.ExamEnrolment.ExamEnrolmentStatus;

public class StudentExamParticipationStatusModel {
	private ExamEnrolmentStatus status;
	private String reason;

	public StudentExamParticipationStatusModel() {}
	
	public StudentExamParticipationStatusModel(ExamEnrolmentStatus status) {
		this.status = status;
	}
	
	public StudentExamParticipationStatusModel(ExamEnrolmentStatus status, String reason) {
		this.status = status;
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setStatus(ExamEnrolmentStatus status) {
		this.status = status;
	}


	public ExamEnrolmentStatus getStatus() {
		return this.status;
	}
}