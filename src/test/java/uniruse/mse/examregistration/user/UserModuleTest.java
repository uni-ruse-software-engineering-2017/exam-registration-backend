package uniruse.mse.examregistration.user;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.SignUpUser;
import uniruse.mse.examregistration.user.model.UserRole;

public class UserModuleTest extends BaseTest {
	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder encoder;

	@Test
	@Ignore
	public void should_CreateUserAccount() throws Exception {
		final Pair<ApplicationUser, String> admin = this.loginAsAdmin();
		final String jwt = admin.getSecond();

		final ApplicationUser testUser = this.getTestUser();
		this.post("/users", this.toJson(testUser), jwt).andExpect(MockMvcResultMatchers.status().isCreated());

		final ApplicationUser createdUser = userService.getByUsername(testUser.getUsername()).get();

		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertTrue("verify password is encrypted", encoder.matches(testUser.getPassword(), createdUser.getPassword()));
		assertEquals(UserRole.STUDENT, createdUser.getRole());
		assertFalse(createdUser.isActive());
	}

	@Test
	@Ignore
	public void should_ReportErrorWhenUserAlreadyExists() throws Exception {
		final Pair<ApplicationUser, String> admin = this.loginAsAdmin();
		final String jwt = admin.getSecond();
		final String testUserJson = toJson(this.getTestUser());

		this.post("/users", testUserJson, jwt);
		this.post("/users", testUserJson, jwt).andExpect(MockMvcResultMatchers.status().isConflict());
	}

	@Test
	public void should_SignUpNewStudent() throws Exception {
		final String username = "s136510@stud.uni-ruse.bg";
		final String password = "secret_pass";

		final SignUpUser newUserData = new SignUpUser() {
			{
				setUsername(username);
				setPassword(password);
			}
		};

		final String jsonBody = toJson(newUserData);
		this.post("/sign-up", jsonBody, "").andExpect(MockMvcResultMatchers.status().isCreated());

		final ApplicationUser signedUpUser = userService.getByUsername(username).get();

		assertEquals(username, signedUpUser.getUsername());
		assertTrue("verify password encrypted", encoder.matches(password, signedUpUser.getPassword()));
		assertEquals(UserRole.STUDENT, signedUpUser.getRole());
		assertFalse(signedUpUser.isActive());
	}

	@Test
	@Ignore
	public void should_SignUpNewTeacher() throws Exception {
		final String username = "p.hristova@ami.uni-ruse.bg";
		final String password = "secret_pass";

		final SignUpUser newUserData = new SignUpUser() {
			{
				setUsername(username);
				setPassword(password);
			}
		};

		final String jsonBody = toJson(newUserData);
		this.post("/sign-up", jsonBody, "").andExpect(MockMvcResultMatchers.status().isCreated());

		final ApplicationUser signedUpUser = userService.getByUsername(username).get();

		assertEquals(username, signedUpUser.getUsername());
		assertTrue("verify password encrypted", encoder.matches(password, signedUpUser.getPassword()));
		assertEquals(UserRole.PROFESSOR, signedUpUser.getRole());
	}

	@Test
	public void should_NotAllowNonUniversityEmailAddresses() throws Exception {
		final String username = "john.doe@gmail.com";
		final String password = "secret_pass";

		final SignUpUser newUserData = new SignUpUser() {
			{
				setUsername(username);
				setPassword(password);
			}
		};

		final String jsonBody = toJson(newUserData);
		this.post("/sign-up", jsonBody, "").andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}

	@Test
	public void should_GetUserProfileForStudents() throws Exception {
		final Pair<ApplicationUser, String> user = this.loginAsStudent();
		final ApplicationUser userObj = user.getFirst();

		this.get("/profile", user.getSecond()).andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(userObj.getUsername())))
				.andExpect(jsonPath("$.fullName", is(userObj.getFullName())));
	}

	@Test
	public void should_ActivateUser() throws Exception {
		final ApplicationUser testUser = createUser("s136510@stud.uni-ruse.bg", "12345678", UserRole.STUDENT);

		final ApplicationUser createdUser = userService.getByUsername(testUser.getUsername()).get();

		final String activationToken = encoder.encode(createdUser.getId() + createdUser.getUsername());

		this.get("/activate/" + createdUser.getUsername() + "?token=" + activationToken, null)
				.andExpect(status().isOk());

		final ApplicationUser activatedUser = userService.getByUsername(createdUser.getUsername()).get();

		assertTrue(activatedUser.isActive());
	}

	@Test
	public void should_ReturnErrorWhenActivatingUserDoesNotExisting() throws Exception {
		this.get("/activate/not_existing_user?token=323232323", null).andExpect(status().isNotFound());
	}

	@Test
	public void should_NotActivateUser_WhenTokenDoesNotMatch() throws Exception {
		final ApplicationUser testUser = createUser("s136510@stud.uni-ruse.bg", "12345678", UserRole.STUDENT);

		final ApplicationUser createdUser = userService.getByUsername(testUser.getUsername()).get();

		this.get("/activate/" + createdUser.getUsername() + "?token=dasdasdasd", null)
				.andExpect(status().isUnprocessableEntity());

		final ApplicationUser activatedUser = userService.getByUsername(createdUser.getUsername()).get();

		assertFalse(activatedUser.isActive());
	}

	@Test
	public void should_ThrowException_WhenUserIsAlreadyActive() throws Exception {
		final ApplicationUser testUser = createUser("s136510@stud.uni-ruse.bg", "12345678", UserRole.STUDENT);

		final ApplicationUser createdUser = userService.getByUsername(testUser.getUsername()).get();

		final String activationToken = userService.generateActicationToken(testUser.getUsername());

		this.get("/activate/" + createdUser.getUsername() + "?token=" + activationToken, null)
				.andExpect(status().isOk());

		this.get("/activate/" + createdUser.getUsername() + "?token=" + activationToken, null)
				.andExpect(status().isUnprocessableEntity());
	}

	private ApplicationUser getTestUser() {
		final ApplicationUser user = new ApplicationUser();
		user.setUsername("s136510@stud.uni-ruse.bg");
		user.setFullName("Tsvetan Ganev");
		user.setPassword("12345678");
		user.setRole(UserRole.STUDENT);
		user.setActive(false);
		return user;
	}

}
