package uniruse.mse.examregistration.user.model;

import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "student")
@PrimaryKeyJoinColumn(name="user_id")
public class Student extends ApplicationUser {
	private static final Pattern FN_REGEX = Pattern.compile("^[0-9]{6}$");

	@Length(min=6, max=6, message="The faculty number is exactly 6 digits long")
	private String facultyNumber;

	@Enumerated(EnumType.STRING)
	private StudyForm studyForm;

	private String specialty;

	private String groupNumber;

	public Student() {
		super();
		this.setRole(UserRole.STUDENT);
	}

	public Student(
		ApplicationUser userData,
		StudyForm studyForm,
		String specialty,
		String groupNumber
	) {

		super(
			userData.getUsername(),
			userData.getPassword(),
			userData.getFullName(),
			UserRole.STUDENT
		);

		this.setActive(userData.isActive());

		// extract the faculty number from the email address
		this.facultyNumber = userData.getUsername().substring(1, 7);
		this.studyForm = studyForm;
		this.specialty = specialty;
		this.groupNumber = groupNumber;
	}

	public String getFacultyNumber() {
		return facultyNumber;
	}

	public void setFacultyNumber(String facultyNumber) {
		if (facultyNumber == null || !FN_REGEX.matcher(facultyNumber).matches()) {
			throw new IllegalArgumentException(facultyNumber + " is not a valid faculty number.");
		}

		this.facultyNumber = facultyNumber;
	}

	public StudyForm getStudyForm() {
		return studyForm;
	}

	public void setStudyForm(StudyForm studyForm) {
		this.studyForm = studyForm;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public String getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(String groupNumber) {
		this.groupNumber = groupNumber;
	}
}
