package uniruse.mse.examregistration.subject;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import javax.transaction.Transactional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.subject.SubjectResource.SubjectAssignmentRequest;
import uniruse.mse.examregistration.user.UserRole;

public class SubjectTests extends BaseTest {

	@Autowired
	private SubjectRepository subjectRepository;

	@Test
	public void should_CreateSubject() throws Exception {
		String subjectJson = fromFile("subject.json");

		this.post("/subjects", subjectJson)
			.andExpect(MockMvcResultMatchers.status()
				.isCreated());

		Subject subject = subjectRepository.findById(1L)
			.get();

		assertEquals("mathematic", subject.getName());
	}

	@Test
	public void should_NotAllowMultimpleSubjectsWithSameName()
			throws Exception {
		String subjectJson = fromFile("subject.json");

		this.post("/subjects", subjectJson);
		this.post("/subjects", subjectJson)
			.andExpect(MockMvcResultMatchers.status()
				.isConflict());
	}

	@Test
	public void should_FetchAllSubjectsOrderedByName() throws Exception {
		String subjectJson = fromFile("subject.json");
		String informaticsSubject = subjectJson.replace("mathematic",
				"informatics");
		this.post("/subjects", informaticsSubject);

		String chemistrySubject = subjectJson.replace("mathematic",
				"chemistry");
		this.post("/subjects", chemistrySubject);

		mockMvc.perform(get("/subjects").contentType(contentType))
			.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	@Transactional
	public void should_AssignProfessorToSubject() throws Exception {
		createUser("grigorova", UserRole.PROFESSOR);
		createUser("hristova", UserRole.PROFESSOR);

		String subjectJson = fromFile("subject.json");
		this.post("/subjects", subjectJson);

		SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova", "hristova" });

		this.patch("/subjects/1/assignees", toJson(request));

		Subject subject = subjectRepository.findById(1L)
			.get();

		assertEquals(2, subject.getProfessors()
			.size());
	}

	@Test
	public void should_NotAssignProfessorWhenSubjectDoesNotExist()
			throws Exception {
		createUser("grigorova", UserRole.PROFESSOR);

		SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova", "hristova" });

		this.patch("/subjects/1/assignees", toJson(request))
			.andExpect(MockMvcResultMatchers.status()
				.isNotFound());
	}

	@Test
	public void should_NotAllowAssigningNonProfessorToSubject()
			throws Exception {
		createUser("grigorova", UserRole.STUDENT);

		String subjectJson = fromFile("subject.json");
		this.post("/subjects", subjectJson);

		SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request))
			.andExpect(MockMvcResultMatchers.status()
				.isUnprocessableEntity());
	}

	@Test
	@Transactional
	public void should_UnassignProfessorToSubject() throws Exception {
		createUser("grigorova", UserRole.PROFESSOR);

		String subjectJson = fromFile("subject.json");
		this.post("/subjects", subjectJson);

		SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setAdded(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request));

		Subject subject = subjectRepository.findById(1L)
			.get();
		assertEquals(1, subject.getProfessors()
			.size());

		request = new SubjectAssignmentRequest();
		request.setRemoved(new String[] { "grigorova" });

		this.patch("/subjects/1/assignees", toJson(request));

		subject = subjectRepository.findById(1L)
			.get();
		assertEquals(0, subject.getProfessors()
			.size());
	}

	@Test
	public void should_NotUnassignProfessorWhenSubjectDoesNotExist()
			throws Exception {
		createUser("grigorova", UserRole.PROFESSOR);

		SubjectAssignmentRequest request = new SubjectAssignmentRequest();
		request.setRemoved(new String[] { "grigorova" });

		this.post("/subjects/1", toJson(request))
			.andExpect(MockMvcResultMatchers.status()
				.isNotFound());
	}

}
