package uniruse.mse.examregistration.exam;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
		return this.examRepository.findAll(new Sort(Direction.DESC, "id"));
	}

	public Exam getById(Long examId) {
		return this.examRepository.findById(examId).orElseThrow(
			() -> new ObjectNotFoundException("Exam with ID " + examId + " was not found.")
		);
	}

	public Exam create(NewExamModel newExam, Professor professor) {
		// TODO: Dates validation
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

	private boolean hasRightsToPublishExam(Professor prof, Subject subjectChosen) {
		if (prof == null || subjectChosen == null) {
			return false;
		}

		return subjectChosen.getProfessors().contains(prof);
	}
}
