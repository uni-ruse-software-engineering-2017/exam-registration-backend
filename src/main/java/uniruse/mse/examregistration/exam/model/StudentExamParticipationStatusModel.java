package uniruse.mse.examregistration.exam.model;

import uniruse.mse.examregistration.exam.ExamParticipationRequest.ExamParticipationRequestStatus;

public class StudentExamParticipationStatusModel {
	public ExamParticipationRequestStatus status;

	public StudentExamParticipationStatusModel() {}

	public StudentExamParticipationStatusModel(ExamParticipationRequestStatus status) {
		this.status = status;
	}

	public ExamParticipationRequestStatus getStatus() {
		return this.status;
	}
}