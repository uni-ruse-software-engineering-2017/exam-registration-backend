package uniruse.mse.examregistration.exam;

import static org.springframework.data.jpa.domain.Specification.where;
import static uniruse.mse.examregistration.exam.ExamSpecifications.hasNotFinished;
import static uniruse.mse.examregistration.exam.ExamSpecifications.startsOn;
import static uniruse.mse.examregistration.exam.ExamSpecifications.withProfessorId;
import static uniruse.mse.examregistration.exam.ExamSpecifications.withSubjectId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.exam.ExamParticipationRequest.ExamParticipationRequestStatus;
import uniruse.mse.examregistration.exam.model.NewExamModel;
import uniruse.mse.examregistration.exam.model.StudentExamParticipationStatusModel;
import uniruse.mse.examregistration.exception.ObjectNotFoundException;
import uniruse.mse.examregistration.exception.OperationNotAllowedException;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.subject.SubjectService;
import uniruse.mse.examregistration.user.model.Professor;
import uniruse.mse.examregistration.user.model.Student;

@Service
public class ExamService {
	@Autowired
	private ExamRepository examRepository;

	@Autowired
	private SubjectService subjectService;

	public List<Exam> getAll() {
		return this.examRepository.findAll(
			hasNotFinished(), new Sort(Direction.ASC, "startTime")
		);
	}

	@Transactional
	public List<Exam> getAll(Long subjectId, Date date, Long professorId) {
		Specification<Exam> criteria = where(hasNotFinished());

		if (subjectId != null && subjectId > 0) {
			criteria = criteria.and(withSubjectId(subjectId));
		}

		if (professorId != null && professorId > 0) {
			criteria = criteria.and(withProfessorId(professorId));
		}

		if (date != null) {
			criteria = criteria.and(startsOn(date));
		}

		return this.examRepository.findAll(
			criteria, new Sort(Direction.ASC, "startTime")
		);
	}

	public Exam getById(Long examId) {
		return this.examRepository.findById(examId).orElseThrow(
			() -> new ObjectNotFoundException("Exam with ID " + examId + " was not found.")
		);
	}

	public Exam create(NewExamModel newExam, Professor professor) {
		LocalDateTime examStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(newExam.getStartTime()), TimeZone.getDefault().toZoneId());
		LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);

		if (examStartTime.isBefore(threeDaysFromNow)) {
			throw new OperationNotAllowedException("Exam can not be created because there are less than three days until the exam starts.");	
		}

		final Exam exam = new Exam();
		exam.setStartTime(new Date(newExam.getStartTime()));
		exam.setEndTime(new Date(newExam.getEndTime()));
		exam.setHall(newExam.getHall());
		exam.setMaxSeats(newExam.getMaxSeats());
		exam.setProfessor(professor);
		final Subject subject = this.subjectService.getSubjectById(newExam.getSubjectId());

		if (subject == null) {
			throw new ObjectNotFoundException("Subject with ID " + newExam.getSubjectId() + " was not found.");
		} 

		if (!hasRightsToPublishExam(professor, subject)) {
			throw new AccessDeniedException("Professor not allowed to create exams for subject " + subject.getName());
		}

		exam.setSubject(subject);

		return this.examRepository.save(exam);
	}

	public Exam update(Long examId, Exam newExamData) {
		final Exam exam = this.getById(examId);
		final LocalDateTime now = LocalDateTime.now();
		LocalDateTime threeDaysBeforeStart = exam.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().minusDays(3);

		if (exam.getParticipationRequests().size() > 0 && now.isAfter(threeDaysBeforeStart)) {
			throw new OperationNotAllowedException("Exam can not be changed because there are less than three days until the exam starts.");
		}
			
		if (newExamData.getHall() != null && newExamData.getHall() != "") {
			exam.setHall(newExamData.getHall());
		}

		if (newExamData.getMaxSeats() > exam.getMaxSeats()) {
			exam.setMaxSeats(newExamData.getMaxSeats());
		}

		if (newExamData.getStartTime() != null) {
			exam.setStartTime(newExamData.getStartTime());
		}

		if (newExamData.getEndTime() != null) {
			exam.setEndTime(newExamData.getEndTime());
		}

		final Exam savedExam = this.examRepository.save(exam);

		return savedExam;
	}

	public void cancel(Long examId) {
		final Exam exam = this.getById(examId);

		examRepository.delete(exam);
	}

	public void applyForExam(Student student, Long examId) {
		// TODO: check exam date
		final Exam exam = this.getById(examId);

		if (exam == null) {
			throw new ObjectNotFoundException(
				"Exam with ID " + examId + " was not found."
			);
		}

		// check if user has already applied for that exam
		if (exam.getParticipationRequests()
				.stream()
				.filter(
					(pr) -> pr.getStudent().getUsername() == student.getUsername())
				.count() > 0
		) {
			throw new OperationNotAllowedException(
				"You have already applied for this exam."
			);
		}

		final ExamParticipationRequest epr = new ExamParticipationRequest();
		epr.setExam(exam);
		epr.setStatus(ExamParticipationRequestStatus.PENDING);
		epr.setStudent(student);

		exam.getParticipationRequests().add(epr);

		examRepository.save(exam);
	}

	public void cancelExamApplication(Student student, Long examId) {
		// TODO: check exam date
		final Exam exam = this.getById(examId);

		if (exam == null) {
			throw new ObjectNotFoundException(
				"Exam with ID " + examId + " was not found."
			);
		}

		// check if user has already applied for that exam
		if (exam.getParticipationRequests()
				.stream()
				.filter(
					(pr) -> pr.getStudent().getUsername() == student.getUsername()
				)
				.count() == 0
		) {
			throw new OperationNotAllowedException(
				"You haven't applied for this exam yet."
			);
		}

		// remove the student's exam application
		exam.setParticipationRequests(
			exam.getParticipationRequests()
				.stream()
				.filter(
					(pr) -> pr.getStudent().getUsername() != student.getUsername()
				)
				.collect(Collectors.toList())
		);

		examRepository.save(exam);
	}

	public Exam changeStudentParticipationStatus(Long studentId, Long examId, StudentExamParticipationStatusModel statusChange, Professor prof) {
		final Exam exam = this.getById(examId);

		if (exam == null) {
			throw new ObjectNotFoundException(
				"Exam with ID " + examId + " was not found."
			);
		}

		if (exam.getProfessor().getUsername() != prof.getUsername()) {
			throw new AccessDeniedException(
				"Exam with ID " + examId + " was not published by you."
			);
		}

		final ExamParticipationRequest participationRequest = exam.getParticipationRequests()
			.stream()
			.filter(pr -> pr.getStudent().getId() == studentId)
			.findFirst()
			.orElseThrow(
				() -> new OperationNotAllowedException("The selected student has not applied for this exam yet")
			);

		if (participationRequest.getStatus() != statusChange.getStatus()) {
			participationRequest.setStatus(statusChange.getStatus());

			if (statusChange.getStatus() == ExamParticipationRequestStatus.REJECTED) {
				participationRequest.setReason(statusChange.getReason());
				if (statusChange.getReason() == null || statusChange.getReason().equals("")) {
					throw new OperationNotAllowedException("You must provide a reason for the rejection");
				}
			}

			return examRepository.save(exam);
		} else {
			throw new OperationNotAllowedException(
				"Student participation status is already set to " + statusChange.getStatus().name() + "."
			);
		}
	}

	private boolean hasExamFinished(Exam exam) {
		final LocalDateTime examFinishesAt = toLocalDateTime(exam.getEndTime());

		if (examFinishesAt.isBefore(LocalDateTime.now())) {
			return true;
		}

		return false;
	}

	private boolean hasRightsToPublishExam(Professor prof, Subject subjectChosen) {
		if (prof == null || subjectChosen == null) {
			return false;
		}

		return subjectChosen.getProfessors().contains(prof);
	}

	private LocalDateTime toLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}
