package uniruse.mse.examregistration.exam;

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

import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.user.model.ApplicationUser;

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

	@ManyToOne(fetch = FetchType.EAGER)
	private ApplicationUser professor;

	@OneToMany(mappedBy = "exam", cascade = CascadeType.REMOVE)
	private List<ExamParticipationRequest> participationRequests;

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

	public ApplicationUser getProfessor() {
		return professor;
	}

	public void setProfessor(ApplicationUser professor) {
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

	public List<ExamParticipationRequest> getParticipationRequests() {
		return participationRequests;
	}

	public void setParticipationRequests(List<ExamParticipationRequest> participationRequests) {
		this.participationRequests = participationRequests;
	}

}
