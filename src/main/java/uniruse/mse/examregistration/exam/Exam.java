package uniruse.mse.examregistration.exam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonProperty;

import uniruse.mse.examregistration.exam.ExamEnrolment.ExamEnrolmentStatus;
import uniruse.mse.examregistration.exception.OperationNotAllowedException;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.Professor;
import uniruse.mse.examregistration.util.DateConverter;

@Entity
@Table(name = "exam")
public class Exam {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

	private String hall;

	private Integer maxSeats;

	@ManyToOne(fetch = FetchType.EAGER)
	private Subject subject;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity=ApplicationUser.class)
	private Professor professor;

	@OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExamEnrolment> enrolledStudents = new ArrayList<>();

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedOn;

	@PrePersist
	void createdAt() {
		createdOn = new Date();
		modifiedOn = new Date();
	}

	@PreUpdate
	void updatedAt() {
		modifiedOn = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getHall() {
		return hall;
	}

	public void setHall(String hall) {
		this.hall = hall;
	}

	public Integer getMaxSeats() {
		return maxSeats;
	}

	public void setMaxSeats(Integer maxSeats) {
		this.maxSeats = maxSeats;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public List<ExamEnrolment> getEnrolledStudents() {
		return enrolledStudents;
	}

	public void setEnrolledStudents(List<ExamEnrolment> enrolledStudents) {
		this.enrolledStudents = enrolledStudents;
	}

	public boolean unenrolStudent(String username) {
		return this.getEnrolledStudents().removeIf(
			enrollment -> enrollment.getStudent().getUsername().equals(username)
		);
	}

	public boolean hasEnrolledStudent(String username) {
		return this.getEnrolledStudents()
		.stream()
		.filter(
			enrolment -> enrolment.getStudent().getUsername().equals(username)
		)
		.count() > 0;
	}

	public ExamEnrolment getEnrolledStudentById(Long studentId) {
		return this.getEnrolledStudents()
		.stream()
		.filter(pr -> pr.getStudent().getId() == studentId)
		.findFirst()
		.orElseThrow(
			() -> new OperationNotAllowedException("The selected student has not applied for this exam yet")
		);
	}

	public boolean hasEnrolledStudents() {
		return this.getEnrolledStudents().size() > 0;
	}

	@JsonProperty("approvedCount")
	public long getApprovedCount() {
		return this.getEnrolledStudents()
				.stream()
				.filter(e -> e.getStatus() == ExamEnrolmentStatus.APPROVED).count();
	}

	@JsonProperty("canStudentsEnrol")
	public boolean canStudentsEnrol() {
		return this.getApprovedCount() < this.getMaxSeats() && this.hasMoreThanThreeDays();
	}

	@JsonProperty("canStudentsUnenrol")
	public boolean canStudentsUnenrol() {
		return this.hasMoreThanThreeDays();
	}

	public boolean hasMoreThanThreeDays() {
		final LocalDateTime examStartTime = DateConverter.toLocalDateTime(getStartTime());
		final LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);

		return examStartTime.isAfter(threeDaysFromNow);
	}
}
