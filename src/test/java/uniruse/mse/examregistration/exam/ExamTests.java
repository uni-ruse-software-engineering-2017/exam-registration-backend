package uniruse.mse.examregistration.exam;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.exam.ExamParticipationRequest.ExamParticipationRequestStatus;
import uniruse.mse.examregistration.exam.model.NewExamModel;
import uniruse.mse.examregistration.exam.model.StudentExamParticipationStatusModel;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.subject.SubjectService;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.Professor;
import uniruse.mse.examregistration.user.model.Student;

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
		LocalDateTime start = LocalDateTime.now().plusDays(5);
		LocalDateTime end = LocalDateTime.now().plusDays(5).plusHours(2);
		
		final NewExamModel model = new NewExamModel(
			maths.getId(),
			start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			"403a",
			25
		);

		final String jsonBody = this.toJson(model);

		this.post(ENDPOINT, jsonBody, profJwt)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.hall").value(model.getHall()))
			.andExpect(jsonPath("$.maxSeats").value(model.getMaxSeats()))
			.andExpect(jsonPath("$.subject.id").value(maths.getId()))
			.andExpect(jsonPath("$.subject.name").value(maths.getName()))
			.andExpect(jsonPath("$.subject.description").hasJsonPath())
			.andExpect(jsonPath("$.professor.username").value(this.prof.getUsername()))
			.andExpect(jsonPath("$.professor.role").value(this.prof.getRole().name()));
	}

	@Test
	public void should_NotCreateExamDateIfProfessorDoesntHaveTheSubjectAssigned() throws Exception {
		final Subject maths = this.createSubject("Maths");
		LocalDateTime start = LocalDateTime.now().plusDays(5);
		LocalDateTime end = LocalDateTime.now().plusDays(5).plusHours(2);
		
		final NewExamModel model = new NewExamModel(
			maths.getId(),
			start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			"403a",
			25
		);
		

		final String jsonBody = this.toJson(model);

		this.post(ENDPOINT, jsonBody, profJwt)
			.andExpect(status().isForbidden());
	}

	@Test
	@Transactional
	public void should_ListFilterExamDatesBySubject() throws Exception {
		final Subject maths = this.createSubject("Maths");
		final Subject informatics = this.createSubject("Informatics");
		subjectService.updateAssignees(maths.getId(), new String[]{ prof.getUsername() }, null);
		subjectService.updateAssignees(informatics.getId(), new String[]{ prof.getUsername() }, null);

		final NewExamModel mathsExam = new NewExamModel(
			maths.getId(),
			1599487245000L, // Monday, 7 September 2020 14:00:45
			1599494445000L, // Monday, 7 September 2020 16:00:45
			"403a",
			25
		);

		final NewExamModel informaticsExam = new NewExamModel(
			informatics.getId(),
			1594116045000L, // Sunday, 7 June 2020 10:00:45
			1594126845000L, // Sunday, 7 June 2020 13:00:45
			"501c",
			15
		);

		this.examService.create(mathsExam, prof);
		this.examService.create(informaticsExam, prof);

		this.get(ENDPOINT + "?subjectId=" + informatics.getId() , profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].subject.name").value(informatics.getName()))
			.andExpect(jsonPath("$[0].hall").value(informaticsExam.getHall()))
			.andExpect(jsonPath("$[0].maxSeats").value(informaticsExam.getMaxSeats()));
	}

	@Test
	@Transactional
	public void should_ListFilterExamDatesByExactStartDate() throws Exception {
		final Subject maths = this.createSubject("Maths");
		final Subject informatics = this.createSubject("Informatics");
		subjectService.updateAssignees(maths.getId(), new String[]{ prof.getUsername() }, null);
		subjectService.updateAssignees(informatics.getId(), new String[]{ prof.getUsername() }, null);

		final NewExamModel mathsExam = new NewExamModel(
			maths.getId(),
			1599487245000L, // Monday, 7 September 2020 14:00:45
			1599494445000L, // Monday, 7 September 2020 16:00:45
			"403a",
			25
		);

		final NewExamModel informaticsExam = new NewExamModel(
			informatics.getId(),
			1594116045000L, // Sunday, 7 June 2020 10:00:45
			1594126845000L, // Sunday, 7 June 2020 13:00:45
			"501c",
			15
		);

		this.examService.create(mathsExam, prof);
		this.examService.create(informaticsExam, prof);

		this.get(ENDPOINT + "?date=07.07.2020" , profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].subject.name").value(informatics.getName()))
			.andExpect(jsonPath("$[0].hall").value(informaticsExam.getHall()))
			.andExpect(jsonPath("$[0].maxSeats").value(informaticsExam.getMaxSeats()));
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
	public void should_Return404WhenTryingToGetNonExistingExamById() throws Exception {
		this.get(ENDPOINT + "/" + 42, profJwt)
			.andExpect(status().isNotFound());
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
	
	// TODO: Fix test
	@Transactional
	public void should_NotUpdateExamDetailsWhenThereAreLessThan3DaysRemaining() throws Exception {
		final Exam createdExam = createTestExam();
		final Student student = createActiveStudent("s136500@ami.uni-ruse.bg", "12345678");

		examService.applyForExam(student, createdExam.getId());

		final Exam examPatch = new Exam();
		examPatch.setHall("101a");
		examPatch.setMaxSeats(30);

		final String httpBody = toJson(examPatch);

		this.patch(ENDPOINT + "/" + createdExam.getId(), httpBody, profJwt)
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@Transactional
	public void should_UpdateStudentParticipationRequestStatusToApproved() throws Exception {
		final Exam createdExam = createTestExam();
		final Student student = createActiveStudent("s136500@ami.uni-ruse.bg", "12345678");

		examService.applyForExam(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamParticipationRequestStatus.APPROVED
		);

		final String httpBody = toJson(status);

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isOk());
	}

	@Test
	@Transactional
	public void should_UpdateStudentParticipationRequestStatusToRejected() throws Exception {
		final Exam createdExam = createTestExam();
		final Student student = createActiveStudent("s136500@ami.uni-ruse.bg", "12345678");

		examService.applyForExam(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamParticipationRequestStatus.REJECTED, "No coursework provided!"
		);

		final String httpBody = toJson(status);

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isOk());
	}

	@Test
	@Transactional
	public void should_NotRejectStudentWithoutProvidingAReason() throws Exception {
		final Exam createdExam = createTestExam();
		final Student student = createActiveStudent("s136500@ami.uni-ruse.bg", "12345678");

		examService.applyForExam(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamParticipationRequestStatus.REJECTED
		);

		final String httpBody = toJson(status);

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@Transactional
	public void should_NotUpdateStudentParticipationRequestStatusForExamPublishedByOtherProfessor() throws Exception {
		final Exam createdExam = createTestExam();
		final Student student = createActiveStudent("s136500@ami.uni-ruse.bg", "12345678");

		examService.applyForExam(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamParticipationRequestStatus.REJECTED
		);

		final String httpBody = toJson(status);

		final Pair<Professor, String> otherProfessor = this.loginAsProfessor("p.petrov@ami.uni-ruse.bg", "12345678");

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, otherProfessor.getSecond())
			.andExpect(status().isForbidden());
	}

	@Test
	@Transactional
	public void should_NotUpdateStudentParticipationRequestStatusWhenTryingToSetTheSameStatus() throws Exception {
		final Exam createdExam = createTestExam();
		final Student student = createActiveStudent("s136500@ami.uni-ruse.bg", "12345678");

		examService.applyForExam(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamParticipationRequestStatus.APPROVED
		);

		final String httpBody = toJson(status);

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isOk());

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isUnprocessableEntity());
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

	@Test
	@Transactional
	public void should_CancelExamApplicationAsStudent() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/apply", "", studentLogin.getSecond())
			.andExpect(status().isOk());

		this.post(ENDPOINT + "/" + createdExam.getId() + "/cancel", "", studentLogin.getSecond())
			.andExpect(status().isOk());

		final Exam updatedExam = this.examService.getById(createdExam.getId());

		assertEquals(0, updatedExam.getParticipationRequests().size());
	}

	@Test
	@Transactional
	public void should_FailToCancelExamApplicationAsStudentIfHaventAppliedBeforehand() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/cancel", "", studentLogin.getSecond())
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

		LocalDateTime start = LocalDateTime.now().plusDays(5);
		LocalDateTime end = LocalDateTime.now().plusDays(5).plusHours(2);
		
		final NewExamModel exam = new NewExamModel(
			maths.getId(),
			start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			"403a",
			25
		);

		final Exam createdExam = this.examService.create(exam, prof);
		return createdExam;
	}

}
