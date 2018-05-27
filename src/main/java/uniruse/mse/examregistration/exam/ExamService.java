package uniruse.mse.examregistration.exam;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.exception.ObjectNotFoundException;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.subject.SubjectService;
import uniruse.mse.examregistration.user.model.Professor;

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

	private boolean hasRightsToPublishExam(Professor prof, Subject subjectChosen) {
		if (prof == null || subjectChosen == null) {
			return false;
		}

		return subjectChosen.getProfessors().contains(prof);
	}
}
