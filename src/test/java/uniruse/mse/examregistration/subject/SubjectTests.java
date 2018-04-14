package uniruse.mse.examregistration.subject;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import javax.transaction.Transactional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.user.ApplicationUser;
import uniruse.mse.examregistration.user.UserRole;
import uniruse.mse.examregistration.user.UserService;

public class SubjectTests extends BaseTest {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private UserService userService;

	@Test
	public void should_CreateSubject() throws Exception {
		String subjectJson = fromFile("subject.json");

		mockMvc.perform(post("/subjects").contentType(contentType).content(subjectJson))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		Subject subject = subjectRepository.findById(1L).get();

		assertEquals("mathematic", subject.getName());
	}

	@Test
	public void should_NotAllowMultimpleSubjectsWithSameName() throws Exception {
		String subjectJson = fromFile("subject.json");

		mockMvc.perform(post("/subjects").contentType(contentType).content(subjectJson));
		mockMvc.perform(post("/subjects").contentType(contentType).content(subjectJson))
				.andExpect(MockMvcResultMatchers.status().isConflict());
	}

	@Test
	public void should_FetchAllSubjectsOrderedByName() throws Exception {
		String subjectJson = fromFile("subject.json");
		String informaticsSubject = subjectJson.replace("mathematic", "informatics");
		mockMvc.perform(post("/subjects").contentType(contentType).content(informaticsSubject));

		String chemistrySubject = subjectJson.replace("mathematic", "chemistry");
		mockMvc.perform(post("/subjects").contentType(contentType).content(chemistrySubject));

		mockMvc.perform(get("/subjects").contentType(contentType))
			.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	@Transactional
	public void should_AssignProfessorToSubject() throws Exception {
		createUser("grigorova", UserRole.PROFESSOR);
		createUser("hristova", UserRole.PROFESSOR);

		String subjectJson = fromFile("subject.json");
		mockMvc.perform(post("/subjects").contentType(contentType).content(subjectJson));

		mockMvc.perform(post("/subjects/1").contentType(contentType).content("[\"grigorova\", \"hristova\"]"));

		Subject subject = subjectRepository.findById(1L).get();

		assertEquals(2, subject.getProfessors().size());
	}

	@Test
	public void should_NotAssignProfessorWhenSubjectDoesNotExist() {

	}

	@Test
	public void should_NotAllowAssigningNonProfessorToSubject() {

	}

	@Test
	public void should_UnAssignProfessorToSubject() {

	}

	private ApplicationUser createUser(String name, UserRole role) {
		ApplicationUser user = new ApplicationUser();
		user.setUsername(name);
		user.setPassword("123456");
		user.setFullName("Test 123");
		user.setRole(role);

		userService.create(user);

		return user;
	}

}
