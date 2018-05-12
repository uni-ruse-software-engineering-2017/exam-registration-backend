package uniruse.mse.examregistration.user;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.SignUpUser;

public class UserModuleTest extends BaseTest {
	@Autowired
	private UserService userService;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Test
	public void should_CreateUserAccount() throws Exception {
		final Pair<ApplicationUser, String> admin = this.loginAsAdmin();
		final String jwt = admin.getSecond();

		final ApplicationUser testUser = this.getTestUser();
		this.post("/users", this.toJson(testUser), jwt).andExpect(MockMvcResultMatchers.status().isCreated());

		final ApplicationUser createdUser = userService.getByUsername(testUser.getUsername()).get();

		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertTrue("verify password is encrypted", encoder.matches(testUser.getPassword(), createdUser.getPassword()));
		assertEquals(UserRole.STUDENT, createdUser.getRole());
	}

	@Test
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
	}

	@Test
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

	private ApplicationUser getTestUser() {
		final ApplicationUser user = new ApplicationUser();
		user.setUsername("s136510@stud.uni-ruse.bg");
		user.setFullName("Tsvetan Ganev");
		user.setPassword("123456");
		user.setRole(UserRole.STUDENT);
		return user;
	}
}
