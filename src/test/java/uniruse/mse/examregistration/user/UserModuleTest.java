package uniruse.mse.examregistration.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import uniruse.mse.examregistration.ExamRegistrationBackendApplication;
import uniruse.mse.examregistration.H2Config;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ExamRegistrationBackendApplication.class, H2Config.class })
@WebAppConfiguration
public class UserModuleTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

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

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
}
