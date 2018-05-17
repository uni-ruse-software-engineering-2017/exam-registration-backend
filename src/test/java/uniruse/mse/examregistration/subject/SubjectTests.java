package uniruse.mse.examregistration.subject;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.subject.SubjectResource.SubjectAssignmentRequest;
import uniruse.mse.examregistration.user.model.UserRole;

public class SubjectTests extends BaseTest {
	private String adminJwt = "";

	@Autowired
	private SubjectRepository subjectRepository;

	/**
	 * Gets administrator JWT for authentication.
	 * Runs before each test case.
	 *
	 * @throws Exception
	 */
	@Before
	public void getAdminJwt() throws Exception {
		adminJwt = this.loginAsAdmin().getSecond();
	}

	@Test
	public void should_CreateSubject() throws Exception {
		final String subjectJson = fromFile("subject.json");

		this.post("/subjects", subjectJson, adminJwt)
			.andExpect(
				MockMvcResultMatchers.status()
				.isCreated()
			);

		final Subject subject = subjectRepository.findById(1L).get();

		assertEquals("mathematic", subject.getName());
	}


	@Test
	public void should_NotAllowMultimpleSubjectsWithSameName()
			throws Exception {
		final String subjectJson = fromFile("subject.json");

		this.post("/subjects", subjectJson, adminJwt);
		this.post("/subjects", subjectJson, adminJwt)
			.andExpect(MockMvcResultMatchers.status()
			.isConflict());
	}

	@Test
	public void should_FetchAllSubjectsOrderedByName() throws Exception {
		final String subjectJson = fromFile("subject.json");
		final String informaticsSubject = subjectJson.replace("mathematic", "informatics");
		this.post("/subjects", informaticsSubject, adminJwt)
			.andExpect(status().isCreated());

		final String chemistrySubject = subjectJson.replace("mathematic", "chemistry");
		this.post("/subjects", chemistrySubject, adminJwt)
			.andExpect(status().isCreated());

		this.get("/subjects", adminJwt)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	@Transactional
	public void should_AssignProfessorToSubject() throws Exception {
		createUser("grigorova", "123456", UserRole.PROFESSOR);
		createUser("hristova", "123456", UserRole.PROFESSOR);

		final String subjectJson = fromFile("subject.json");
		this.post("/subjects", subjectJson, adminJwt);

		final SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova", "hristova" });

		this.patch("/subjects/1/assignees", toJson(request), adminJwt);

		final Subject subject = subjectRepository.findById(1L).get();

		assertEquals(2, subject.getProfessors().size());
	}

	@Test
	public void should_NotAssignProfessorWhenSubjectDoesNotExist() throws Exception {
		createUser("grigorova", "123456", UserRole.PROFESSOR);

		final SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova", "hristova" });

		this.patch("/subjects/1/assignees", toJson(request), adminJwt)
			.andExpect(status().isNotFound());
	}

	@Test
	public void should_NotAllowAssigningNonProfessorToSubject() throws Exception {
		createUser("grigorova", "123456", UserRole.STUDENT);

		final String subjectJson = fromFile("subject.json");
		this.post("/subjects", subjectJson, adminJwt);

		final SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request), adminJwt)
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@Transactional
	public void should_UnassignProfessorToSubject() throws Exception {
		createUser("grigorova", "123456", UserRole.PROFESSOR);

		final String subjectJson = fromFile("subject.json");
		this.post("/subjects", subjectJson, adminJwt);

		SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request), adminJwt);

		Subject subject = subjectRepository.findById(1L).get();
		assertEquals(1, subject.getProfessors().size());

		request = new SubjectAssignmentRequest();
		request.setRemoved(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request), adminJwt);

		subject = subjectRepository.findById(1L).get();
		assertEquals(0, subject.getProfessors().size());
	}

	@Test
	public void should_NotUnassignProfessorWhenSubjectDoesNotExist() throws Exception {
		createUser("grigorova", "123456", UserRole.PROFESSOR);

		final SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setRemoved(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request), adminJwt)
			.andExpect(MockMvcResultMatchers.status()
			.isNotFound());
	}

}
