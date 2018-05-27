package uniruse.mse.examregistration.exam;

public class NewExamModel {
	private Long subjectId;
	private Long startTime;
	private Long endTime;
	private String hall;
	private Integer maxSeats;

	public NewExamModel(
		Long subjectId,
		Long startTime,
		Long endTime,
		String hall,
		Integer maxSeats
	) {
		if (endTime <= startTime) {
			throw new IllegalArgumentException("End time must be larger than start time.");
		}

		this.subjectId = subjectId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.hall = hall;
		this.maxSeats = maxSeats;
	}

	public NewExamModel() {}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
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
}
