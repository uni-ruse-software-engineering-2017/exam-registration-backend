package uniruse.mse.examregistration.rusystem;

public class RuStudentData {
	//Факултетен номер;Три имена;Форма на обучение (ред/зад/дист);Име на специалност;Група
	private String facultyNumber;

	private String fullName;

	private String specialty;

	private String groupNumber;

	private Integer studyForm;

	public RuStudentData() {}

	public RuStudentData(String facultyNumber, String fullName, String specialty, String groupNumber, Integer studyForm) {
		super();
		this.facultyNumber = facultyNumber;
		this.fullName = fullName;
		this.specialty = specialty;
		this.groupNumber = groupNumber;
		this.studyForm = studyForm;
	}

	public String getFacultyNumber() {
		return facultyNumber;
	}

	public void setFacultyNumber(String facultyNumber) {
		this.facultyNumber = facultyNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public Integer getStudyForm() {
		return studyForm;
	}

	public void setStudyForm(Integer studyForm) {
		this.studyForm = studyForm;
	}
}
