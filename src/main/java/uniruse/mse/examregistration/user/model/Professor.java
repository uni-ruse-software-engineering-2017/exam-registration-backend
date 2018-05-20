package uniruse.mse.examregistration.user.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import uniruse.mse.examregistration.subject.Subject;

@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name="user_id")
public class Professor extends ApplicationUser {
	private String cabinet;

	private String phoneNumber;

	@ManyToMany
	@JoinTable(
		name = "subject_professor",
		joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "subject_id", referencedColumnName = "id")
	)
	private List<Subject> subjectsTeaching;

	public Professor() {
		super();
	}

	public Professor(ApplicationUser userData, String cabinet, String phoneNumber) {
		super(
			userData.getUsername(),
			userData.getPassword(),
			userData.getFullName(),
			UserRole.PROFESSOR
		);

		this.setActive(userData.isActive());

		this.cabinet = cabinet;

		// TODO: Validate the phone number
		this.phoneNumber = phoneNumber;
	}

	public String getCabinet() {
		return cabinet;
	}

	public void setCabinet(String cabinet) {
		this.cabinet = cabinet;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public List<Subject> getSubjectsTeaching() {
		return this.getSubjectsTeaching().stream().collect(Collectors.toList());
	}

	public List<Subject> addSubjectTeaching(Subject subj) {
		this.subjectsTeaching.add(subj);
		return this.getSubjectsTeaching();
	}

	public List<Subject> removeSubjectsTeaching(Subject subj) {
		this.subjectsTeaching.remove(subj);
		return this.getSubjectsTeaching();
	}
}
