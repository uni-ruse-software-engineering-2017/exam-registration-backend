package uniruse.mse.examregistration.exam;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import uniruse.mse.examregistration.user.ApplicationUser;

@Entity
@Table(name = "exam_participation_request")
public class ExamParticipationRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "exam_id")
	private Exam exam;

	@ManyToOne
	@JoinColumn(name = "student_id")
	private ApplicationUser student;

	@Enumerated(EnumType.STRING)
	private ExamParticipationRequestStatus status;

	public Exam getExam() {
		return exam;
	}

	public void setExam(Exam exam) {
		this.exam = exam;
	}

	public ApplicationUser getStudent() {
		return student;
	}

	public void setStudent(ApplicationUser student) {
		this.student = student;
	}

	public ExamParticipationRequestStatus getStatus() {
		return status;
	}

	public void setStatus(ExamParticipationRequestStatus status) {
		this.status = status;
	}

	public static enum ExamParticipationRequestStatus {
		PENDING, APPROVED, REJECTED
	}
}
