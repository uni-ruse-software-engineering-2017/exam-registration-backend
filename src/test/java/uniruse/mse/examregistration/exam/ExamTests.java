package uniruse.mse.examregistration.exam;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.subject.SubjectService;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.Professor;

public class ExamTests extends BaseTest {
	private final static String ENDPOINT = "/exams";

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private ExamService examService;

	private Professor prof = null;
	private String profJwt = "";

	@Before
	public void getProfessorData() throws Exception {
		final Pair<Professor, String> profLoginResult = this.loginAsProfessor();
		this.prof = profLoginResult.getFirst();
		this.profJwt = profLoginResult.getSecond();
	}

	@Test
	@Transactional
	public void should_CreateNewExamDate() throws Exception {
		final Subject maths = this.createSubject("Maths");
		subjectService.updateAssignees(maths.getId(), new String[]{ prof.getUsername() }, null);

		final NewExamModel model = new NewExamModel(
			maths.getId(),
			Instant.now().toEpochMilli(),
			Instant.now().toEpochMilli() + 3600L * 1000,
			"403a",
			25
		);

		final String jsonBody = this.toJson(model);

		this.post(ENDPOINT, jsonBody, profJwt)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.hall").value("403a"))
			.andExpect(jsonPath("$.maxSeats").value(25))
			.andExpect(jsonPath("$.subject.id").value(maths.getId()))
			.andExpect(jsonPath("$.subject.name").value(maths.getName()))
			.andExpect(jsonPath("$.subject.description").hasJsonPath())
			.andExpect(jsonPath("$.professor.username").value(this.prof.getUsername()))
			.andExpect(jsonPath("$.professor.role").value(this.prof.getRole().name()));
	}

	@Test
	public void should_NotCreateExamDateIfProfessorDoesntHaveTheSubjectAssigned() throws Exception {
		final Subject maths = this.createSubject("Maths");

		final NewExamModel model = new NewExamModel(
			maths.getId(),
			Instant.now().toEpochMilli(),
			Instant.now().toEpochMilli() + 3600L * 1000,
			"403a",
			25
		);

		final String jsonBody = this.toJson(model);

		this.post(ENDPOINT, jsonBody, profJwt)
			.andExpect(status().isForbidden());
	}

	@Test
	@Transactional
	public void should_ListAllExamDates() throws Exception {
		final Subject maths = this.createSubject("Maths");
		subjectService.updateAssignees(maths.getId(), new String[]{ prof.getUsername() }, null);

		final NewExamModel exam1 = new NewExamModel(
			maths.getId(),
			Instant.now().toEpochMilli(),
			Instant.now().toEpochMilli() + 3600L * 1000,
			"403a",
			25
		);

		final NewExamModel exam2 = new NewExamModel(
			maths.getId(),
			Instant.now().toEpochMilli(),
			Instant.now().toEpochMilli() + 3600L * 1000,
			"501c",
			15
		);

		this.examService.create(exam1, prof);
		this.examService.create(exam2, prof);

		// results are sorted by ID in descending order (latest records are shown first)
		this.get(ENDPOINT, profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].subject.name").value("Maths"))
			.andExpect(jsonPath("$[0].hall").value("501c"))
			.andExpect(jsonPath("$[0].maxSeats").value(15))
			.andExpect(jsonPath("$[1].subject.name").value("Maths"))
			.andExpect(jsonPath("$[1].hall").value("403a"))
			.andExpect(jsonPath("$[1].maxSeats").value(25));
	}

	@Test
	@Transactional
	public void should_GetExamById() throws Exception {
		final Exam createdExam = createTestExam();

		this.get(ENDPOINT + "/" + createdExam.getId(), profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(createdExam.getId()))
			.andExpect(jsonPath("$.subject.name").value(createdExam.getSubject().getName()))
			.andExpect(jsonPath("$.hall").value(createdExam.getHall()))
			.andExpect(jsonPath("$.maxSeats").value(createdExam.getMaxSeats()));
	}

	@Test
	@Transactional
	public void should_UpdateExamDetails() throws Exception {
		final Exam createdExam = createTestExam();

		final Exam examPatch = new Exam();
		examPatch.setHall("101a");
		examPatch.setMaxSeats(30);

		final String httpBody = toJson(examPatch);

		this.patch(ENDPOINT + "/" + createdExam.getId(), httpBody, profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(createdExam.getId()))
			.andExpect(jsonPath("$.subject.name").value(createdExam.getSubject().getName()))
			.andExpect(jsonPath("$.hall").value(examPatch.getHall()))
			.andExpect(jsonPath("$.maxSeats").value(examPatch.getMaxSeats()));
	}

	@Test
	@Transactional
	public void should_CancelExam() throws Exception {
		final Exam createdExam = createTestExam();

		this.delete(ENDPOINT + "/" + createdExam.getId(), profJwt)
			.andExpect(status().isNoContent());

		this.get(ENDPOINT + "/" + createdExam.getId(), profJwt)
			.andExpect(status().isNotFound());
	}


	@Test
	@Transactional
	public void should_ApplyForExamAsStudent() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/apply", "", studentLogin.getSecond())
			.andExpect(status().isOk());
	}

	@Test
	@Transactional
	public void should_NotBeAbleToApplyForExamAsStudentIfAlreadyApplied() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/apply", "", studentLogin.getSecond())
			.andExpect(status().isOk());

		this.post(ENDPOINT + "/" + createdExam.getId() + "/apply", "", studentLogin.getSecond())
			.andExpect(status().isUnprocessableEntity());

	}

	private Subject createSubject(String name) {
		final Subject subject = new Subject();
		subject.setName(name);

		subjectService.create(subject);

		return subject;
	}

	private Exam createTestExam() {
		final Subject maths = this.createSubject("Maths");
		subjectService.updateAssignees(maths.getId(), new String[]{ prof.getUsername() }, null);

		final NewExamModel exam = new NewExamModel(
			maths.getId(),
			Instant.now().toEpochMilli(),
			Instant.now().toEpochMilli() + 3600L * 1000,
			"403a",
			25
		);

		final Exam createdExam = this.examService.create(exam, prof);
		return createdExam;
	}

}
