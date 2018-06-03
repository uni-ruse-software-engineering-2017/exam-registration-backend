package uniruse.mse.examregistration.exam.model;

import uniruse.mse.examregistration.exam.ExamParticipationRequest.ExamParticipationRequestStatus;

public class StudentExamParticipationStatusModel {
	private ExamParticipationRequestStatus status;
	private String reason;

	public StudentExamParticipationStatusModel() {}
	
	public StudentExamParticipationStatusModel(ExamParticipationRequestStatus status) {
		this.status = status;
	}
	
	public StudentExamParticipationStatusModel(ExamParticipationRequestStatus status, String reason) {
		this.status = status;
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setStatus(ExamParticipationRequestStatus status) {
		this.status = status;
	}


	public ExamParticipationRequestStatus getStatus() {
		return this.status;
	}
}