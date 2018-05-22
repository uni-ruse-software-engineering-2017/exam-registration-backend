package uniruse.mse.examregistration.user.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import uniruse.mse.examregistration.subject.Subject;

@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name="user_id")
public class Professor extends ApplicationUser implements Serializable {
	private static final long serialVersionUID = 4331258974925880125L;

	private String cabinet;

	private String phoneNumber;

	@ManyToMany(mappedBy="professors", fetch=FetchType.EAGER)
	private List<Subject> subjectsTeaching = new ArrayList<>();

	public Professor() {
		super();
		this.setRole(UserRole.PROFESSOR);
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
		return this.subjectsTeaching.stream().collect(Collectors.toList());
	}

	public void setSubjectsTeaching(List<Subject> subjects) {
		if (subjects == null) {
			subjects = new ArrayList<>();
		}

		this.subjectsTeaching = subjects;
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
