package uniruse.mse.examregistration.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uniruse.mse.examregistration.BaseTest;

public class UserModuleTest extends BaseTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Test
	public void should_CreateUserAccount() throws Exception {
		String userJson = "{ \"username\" : \"user1\", \"password\": \"123\","
				+ "\"fullName\": \"User 1\", \"role\": \"STUDENT\"  }";

		mockMvc.perform(post("/users").contentType(contentType).content(userJson))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		ApplicationUser user = userRepository.findById(1L).get();

		assertEquals("user1", user.getUsername());
		assertTrue("verify password encrypted", encoder.matches("123", user.getPassword()));
	}

	@Test
	public void should_ReportErrorWhenUserAlreadyExists() throws Exception {
		String userJson = "{ \"username\" : \"user1\", \"password\": \"123\","
				+ "\"fullName\": \"User 1\", \"role\": \"STUDENT\"  }";

		mockMvc.perform(post("/users").contentType(contentType).content(userJson));
		mockMvc.perform(post("/users").contentType(contentType).content(userJson))
				.andExpect(MockMvcResultMatchers.status().isConflict());
	}

}
