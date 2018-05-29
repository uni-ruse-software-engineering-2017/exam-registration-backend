package uniruse.mse.examregistration.exam;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uniruse.mse.examregistration.exception.ObjectNotFoundException;
import uniruse.mse.examregistration.user.UserService;
import uniruse.mse.examregistration.user.model.Professor;
import uniruse.mse.examregistration.user.model.Student;

@RestController
@RequestMapping("/exams")
public class ExamResource {

	@Autowired
	private ExamService examService;

	@Autowired
	private UserService userService;

	@RequestMapping(method = GET)
	@ResponseBody
	public List<Exam> getExams() {
		final List<Exam> all = this.examService.getAll();

		all.size();
		return all;
	}

	@RequestMapping(method = GET, path="/{examId}")
	public Exam getExamById(@PathVariable Long examId) {
		final Exam exam = this.examService.getById(examId);

		if (exam == null) {
			throw new ObjectNotFoundException(
				"Exam with ID " + examId + " was not found."
			);
		}

		return exam;
	}

	@RequestMapping(method = POST)
	@ResponseStatus(code=HttpStatus.CREATED)
	@PreAuthorize("hasRole('PROFESSOR')")
	public Exam createExam(@RequestBody NewExamModel examData, Authentication auth) {
		final Professor currentProf = (Professor) this.userService
			.getByUsername(auth.getName())
			.orElseThrow(() -> new ObjectNotFoundException(
				"Professor with username " + auth.getName() + " was not found.")
			);

		return this.examService.create(examData, currentProf);
	}

	@RequestMapping(method = PATCH, path="/{examId}")
	@PreAuthorize("hasRole('PROFESSOR')")
	public Exam updateExam(@RequestBody Exam examData, Authentication auth, @PathVariable Long examId) {
		final Professor currentProf = (Professor) this.userService
			.getByUsername(auth.getName())
			.orElseThrow(() -> new ObjectNotFoundException(
				"Professor with username " + auth.getName() + " was not found."
			));

		final Exam exam = this.examService.getById(examId);

		if (exam == null) {
			throw new ObjectNotFoundException(
				"Exam with ID " + examId + " was not found."
			);
		}

		if (exam.getProfessor() != currentProf) {
			throw new AccessDeniedException(
				"You are not allowed to edit the exam details because it is published by " + exam.getProfessor().getUsername()
			);
		}

		return this.examService.update(examId, examData);
	}

	@RequestMapping(method = DELETE, path="/{examId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('PROFESSOR')")
	public void deleteExam(@PathVariable Long examId, Authentication auth) {
		final Professor currentProf = (Professor) this.userService
			.getByUsername(auth.getName())
			.orElseThrow(() -> new ObjectNotFoundException(
				"Professor with username " + auth.getName() + " was not found."
			));

		final Exam exam = this.examService.getById(examId);

		if (exam == null) {
			throw new ObjectNotFoundException(
				"Exam with ID " + examId + " was not found."
			);
		}

		if (exam.getProfessor() != currentProf) {
			throw new AccessDeniedException(
				"You are not allowed to cancel the exam date because it is published by " + exam.getProfessor().getUsername()
			);
		}

		this.examService.cancel(examId);
		return;
	}

	@RequestMapping(method = POST, path="/{examId}/apply")
	@PreAuthorize("hasRole('STUDENT')")
	public void applyForExam(@PathVariable Long examId, Authentication auth) {
		final Student currentStudent = (Student) this.userService
			.getByUsername(auth.getName())
			.orElseThrow(() -> new ObjectNotFoundException(
				"Student with username " + auth.getName() + " was not found."
			));

		this.examService.applyForExam(currentStudent, examId);

		return;
	}
}