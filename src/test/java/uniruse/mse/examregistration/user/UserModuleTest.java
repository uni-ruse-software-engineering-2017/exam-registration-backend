package uniruse.mse.examregistration.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.SignUpUser;

public class UserModuleTest extends BaseTest {
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Test
	public void should_CreateUserAccount() throws Exception {
		String userJson = "{ \"username\" : \"user1\", \"password\": \"123\","
				+ "\"fullName\": \"User 1\", \"role\": \"STUDENT\"  }";

		this.post("/users", userJson)
			.andExpect(MockMvcResultMatchers.status()
				.isCreated());

		ApplicationUser user = userRepository.findById(1L)
			.get();

		assertEquals("user1", user.getUsername());
		assertTrue("verify password encrypted",
				encoder.matches("123", user.getPassword()));
	}

	@Test
	public void should_ReportErrorWhenUserAlreadyExists() throws Exception {
		String userJson = "{ \"username\" : \"user1\", \"password\": \"123\","
				+ "\"fullName\": \"User 1\", \"role\": \"STUDENT\"  }";

		this.post("/users", userJson);
		this.post("/users", userJson)
			.andExpect(MockMvcResultMatchers.status()
				.isConflict());
	}

	@Test
	public void should_SignUpNewStudent() throws Exception {
		final String username = "s136510@stud.uni-ruse.bg";
		final String fullName = "Tsvetan Ganev";
		final String password = "secret_pass";

		SignUpUser newUserData = new SignUpUser() {
			{
				setUsername(username);
				setPassword(password);
				setFullName(fullName);
			}
		};

		String jsonBody = toJson(newUserData);
		this.post("/sign-up", jsonBody)
			.andExpect(MockMvcResultMatchers.status()
				.isCreated());

		ApplicationUser signedUpUser = userService.getByUsername(username)
			.get();

		assertEquals(username, signedUpUser.getUsername());
		assertEquals(fullName, signedUpUser.getFullName());
		assertTrue("verify password encrypted",
				encoder.matches(password, signedUpUser.getPassword()));
		assertEquals(UserRole.STUDENT, signedUpUser.getRole());
	}

	@Test
	public void should_SignUpNewTeacher() throws Exception {
		final String username = "p.hristova@ami.uni-ruse.bg";
		final String fullName = "Plamenka Hristova";
		final String password = "secret_pass";

		SignUpUser newUserData = new SignUpUser() {
			{
				setUsername(username);
				setPassword(password);
				setFullName(fullName);
			}
		};

		String jsonBody = toJson(newUserData);
		this.post("/sign-up", jsonBody)
			.andExpect(MockMvcResultMatchers.status()
				.isCreated());

		ApplicationUser signedUpUser = userService.getByUsername(username)
			.get();

		assertEquals(username, signedUpUser.getUsername());
		assertEquals(fullName, signedUpUser.getFullName());
		assertTrue("verify password encrypted",
				encoder.matches(password, signedUpUser.getPassword()));
		assertEquals(UserRole.PROFESSOR, signedUpUser.getRole());
	}

	@Test
	public void should_NotAllowNonUniversityEmailAddresses() throws Exception {
		final String username = "john.doe@gmail.com";
		final String fullName = "John Doe";
		final String password = "secret_pass";

		SignUpUser newUserData = new SignUpUser() {
			{
				setUsername(username);
				setPassword(password);
				setFullName(fullName);
			}
		};

		String jsonBody = toJson(newUserData);
		this.post("/sign-up", jsonBody)
			.andExpect(MockMvcResultMatchers.status()
				.isUnprocessableEntity());
	}
}
