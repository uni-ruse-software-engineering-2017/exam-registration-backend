package uniruse.mse.examregistration.exam;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.exam.ExamEnrolment.ExamEnrolmentStatus;
import uniruse.mse.examregistration.exam.model.NewExamModel;
import uniruse.mse.examregistration.exam.model.StudentExamParticipationStatusModel;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.subject.SubjectService;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.Professor;
import uniruse.mse.examregistration.user.model.Student;
import uniruse.mse.examregistration.util.DateConverter;

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
		final LocalDateTime start = LocalDateTime.now().plusDays(5);
		final LocalDateTime end = LocalDateTime.now().plusDays(5).plusHours(2);

		final NewExamModel model = new NewExamModel(
			maths.getId(),
			DateConverter.toUnixTimestamp(start),
			DateConverter.toUnixTimestamp(end),
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
		final LocalDateTime start = LocalDateTime.now().plusDays(5);
		final LocalDateTime end = LocalDateTime.now().plusDays(5).plusHours(2);

		final NewExamModel model = new NewExamModel(
			maths.getId(),
			DateConverter.toUnixTimestamp(start),
			DateConverter.toUnixTimestamp(end),
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
	public void should_ShouldCancelExamDate() throws Exception {
		final Exam createdExam = createTestExam();
		final Long examId = createdExam.getId();

		this.delete(ENDPOINT + "/" + examId , profJwt).andExpect(status().is(204));
	}

	@Test
	@Transactional
	public void should_ListUpcomingExamsForStudents() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		// verify that there won't be any "upcoming exams" when
		// the student hasn't enrolled to any exams yet
		this.get(ENDPOINT + "/upcoming", studentLogin.getSecond())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());

		// verify that the exam which the student enrolled for will show up
		this.examService.enrol((Student) studentLogin.getFirst(), createdExam.getId());
		this.get(ENDPOINT + "/upcoming", studentLogin.getSecond())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isNotEmpty())
			.andExpect(jsonPath("$.[0].id").value(createdExam.getId()))
			.andExpect(jsonPath("$.[0].subject.name").value(createdExam.getSubject().getName()))
			.andExpect(jsonPath("$.[0].professor.username").value(createdExam.getProfessor().getUsername()))
			.andExpect(jsonPath("$.[1]").doesNotExist());


		// verify that after unenrolment the student won't see any upcoming exams
		this.examService.unenrol((Student) studentLogin.getFirst(), createdExam.getId());
		this.get(ENDPOINT + "/upcoming", studentLogin.getSecond())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());
	}

	// TODO: Fix this test. It succeeds if run by itself but fails if run along others.
	@Transactional
	public void should_ListUpcomingExamsForProfessors() throws Exception {
		// verify there are no upcoming exams listed
		this.get(ENDPOINT + "/upcoming", profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());

		// verify that the only created exam is listed
		final Exam createdExam = createTestExam();
		final Long examId = createdExam.getId();

		this.get(ENDPOINT + "/upcoming", profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isNotEmpty())
			.andExpect(jsonPath("$.[0].id").value(examId))
			.andExpect(jsonPath("$.[0].subject.name").value(createdExam.getSubject().getName()))
			.andExpect(jsonPath("$.[0].professor.username").value(createdExam.getProfessor().getUsername()))
			.andExpect(jsonPath("$.[1]").doesNotExist());

		// verify that no exams are listed when the exam is cancelled
		this.examService.cancel(examId,(Professor) createdExam.getProfessor());
		this.get(ENDPOINT + "/upcoming", profJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());
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

		examService.enrol(student, createdExam.getId());

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

		examService.enrol(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamEnrolmentStatus.APPROVED
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

		examService.enrol(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamEnrolmentStatus.REJECTED, "No coursework provided!"
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

		examService.enrol(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamEnrolmentStatus.REJECTED
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

		examService.enrol(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamEnrolmentStatus.REJECTED
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

		examService.enrol(student, createdExam.getId());

		final StudentExamParticipationStatusModel status = new StudentExamParticipationStatusModel(
			ExamEnrolmentStatus.APPROVED
		);

		final String httpBody = toJson(status);

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isOk());

		this.patch(ENDPOINT + "/" + createdExam.getId() + "/student/" + student.getId(), httpBody, profJwt)
			.andExpect(status().isUnprocessableEntity());
	}


	@Test
	@Transactional
	public void should_StudentShouldCancelFromExamParticipation() throws Exception {
		final Exam createdExam = createTestExam();

		this.delete(ENDPOINT + "/" + createdExam.getId(), profJwt)
			.andExpect(status().isNoContent());

		this.get(ENDPOINT + "/" + createdExam.getId(), profJwt)
			.andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	public void should_EnrolForExamAsStudent() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/enrol", "", studentLogin.getSecond())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enrolledStudents[0].student.username").value(
				studentLogin.getFirst().getUsername()
			));
	}

	@Test
	@Transactional
	public void should_NotBeAbleToEnrolForExamAsStudentIfAlreadyEnrolled() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/enrol", "", studentLogin.getSecond())
			.andExpect(status().isOk());

		this.post(ENDPOINT + "/" + createdExam.getId() + "/enrol", "", studentLogin.getSecond())
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@Transactional
	public void should_UnenrolForExamAsStudent() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/enrol", "", studentLogin.getSecond())
			.andExpect(status().isOk());

		this.post(ENDPOINT + "/" + createdExam.getId() + "/unenrol", "", studentLogin.getSecond())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enrolledStudents").isEmpty());
	}

	@Test
	@Transactional
	public void should_FailToUnenrolAsStudentIfHaventEnrolledBeforehand() throws Exception {
		final Exam createdExam = createTestExam();
		final Pair<ApplicationUser, String> studentLogin = this.loginAsStudent();

		this.post(ENDPOINT + "/" + createdExam.getId() + "/unenrol", "", studentLogin.getSecond())
			.andExpect(status().isUnprocessableEntity());
	}

	private Subject createSubject(String name) {
		final Subject subject = new Subject();
		subject.setName(name);

		subjectService.create(subject);

		return subject;
	}

	@Transactional
	private Exam createTestExam() {
		final Subject maths = this.createSubject("Maths");
		subjectService.updateAssignees(maths.getId(), new String[]{ prof.getUsername() }, null);

		final LocalDateTime start = LocalDateTime.now().plusDays(5);
		final LocalDateTime end = LocalDateTime.now().plusDays(5).plusHours(2);

		final NewExamModel exam = new NewExamModel(
			maths.getId(),
			DateConverter.toUnixTimestamp(start),
			DateConverter.toUnixTimestamp(end),
			"403a",
			25
		);

		final Exam createdExam = this.examService.create(exam, prof);
		return createdExam;
	}

}
