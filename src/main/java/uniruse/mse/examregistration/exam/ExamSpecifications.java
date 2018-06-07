package uniruse.mse.examregistration.exam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

public class ExamSpecifications {
	public static Specification<Exam> hasFinished() {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.lessThan(root.get("endTime"), new Date());
	}

	public static Specification<Exam> hasNotFinished() {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.greaterThan(root.get("endTime"), new Date());
	}

	public static Specification<Exam> startsOn(Date date) {
		return (root, query, criteriaBuilder) -> {
			final LocalDateTime startOfDay = toLocalDateTime(date).withHour(0).withMinute(0);
			final LocalDateTime endOfDay = toLocalDateTime(date).withHour(23).withMinute(59);
			return criteriaBuilder.between(root.<Date>get("startTime").as(java.util.Date.class), toDate(startOfDay), toDate(endOfDay));
		};
	}

	public static Specification<Exam> withSubjectId(Long subjectId) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("subject"), subjectId);
	}

	public static Specification<Exam> withProfessorId(Long professorId) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("professor"), professorId);
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	private static Date toDate(LocalDateTime dateToConvert) {
	    return java.sql.Timestamp.valueOf(dateToConvert);
	}
}
