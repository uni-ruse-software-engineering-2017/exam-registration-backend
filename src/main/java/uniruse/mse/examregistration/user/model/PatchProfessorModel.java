package uniruse.mse.examregistration.user.model;

public class PatchProfessorModel {

	private String cabinet;


	private String phoneNumber;

	private String fullName;

	public PatchProfessorModel() {}

	public PatchProfessorModel(String cabinet, String phoneNumber, String fullName) {
		super();
		this.cabinet = cabinet;
		this.phoneNumber = phoneNumber;
		this.fullName = fullName;
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
