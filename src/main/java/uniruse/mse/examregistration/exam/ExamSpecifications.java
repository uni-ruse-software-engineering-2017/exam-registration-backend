package uniruse.mse.examregistration.exam;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.criteria.Join;

import org.springframework.data.jpa.domain.Specification;

import uniruse.mse.examregistration.user.model.Student;
import uniruse.mse.examregistration.util.DateConverter;

public class ExamSpecifications {
	public static Specification<Exam> hasStarted() {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.lessThan(root.get("startTime"), new Date());
	}

	public static Specification<Exam> hasNotStarted() {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.greaterThan(root.get("startTime"), new Date());
	}

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
			final LocalDateTime startOfDay = DateConverter.toLocalDateTime(date).withHour(0).withMinute(0);
			final LocalDateTime endOfDay = DateConverter.toLocalDateTime(date).withHour(23).withMinute(59);
			return criteriaBuilder.between(
				root.<Date>get("startTime").as(java.util.Date.class),
				DateConverter.fromLocalDateTime(startOfDay),
				DateConverter.fromLocalDateTime(endOfDay)
			);
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

	public static Specification<Exam> hasEnrolledStudentWithId(Student student) {
		return (root, query, criteriaBuilder) -> {
			final Join<Exam, ExamEnrolment> enrolments = root.join("enrolledStudents");
			return criteriaBuilder.equal(enrolments.get("student").get("id"), student.getId());
		};
	}
}
