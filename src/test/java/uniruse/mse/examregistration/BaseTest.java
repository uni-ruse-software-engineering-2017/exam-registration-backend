package uniruse.mse.examregistration;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uniruse.mse.examregistration.user.UserRole;
import uniruse.mse.examregistration.user.UserService;
import uniruse.mse.examregistration.user.model.ApplicationUser;

@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = { ExamRegistrationBackendApplication.class, H2Config.class })
@WebAppConfiguration
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
		scripts = "classpath:/uniruse/mse/examregistration/truncate_tables.sql")
public abstract class BaseTest {

	protected MockMvc mockMvc;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Autowired
	protected EntityManager em;

	@Autowired
	private UserService userService;

	protected MediaType contentType = new MediaType(
			MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	protected String fromFile(String path) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(path));
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
	}

	@Before
	@Transactional
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.build();
	}

	/**
	 * Helper method for dispatching HTTP POST requests with application/json
	 * content and response type.
	 *
	 * @param url
	 *            - end point URI
	 * @param jsonBody
	 *            - HTTP JSON body
	 * @return HTTP application/json response
	 * @throws Exception
	 */
	protected ResultActions post(String url, String jsonBody) throws Exception {

		return this.mockMvc.perform(MockMvcRequestBuilders.post(url)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonBody));
	}

	/**
	 * Helper method for dispatching HTTP PATCH requests with application/json
	 * content and response type.
	 *
	 * @param url
	 *            - end point URI
	 * @param jsonBody
	 *            - HTTP JSON body
	 * @return HTTP application/json response
	 * @throws Exception
	 */
	protected ResultActions patch(String url, String jsonBody)
			throws Exception {

		return this.mockMvc.perform(MockMvcRequestBuilders.patch(url)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonBody));
	}

	protected String toJson(Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected ApplicationUser createUser(String name, UserRole role) {
		ApplicationUser user = new ApplicationUser();
		user.setUsername(name);
		user.setPassword("123456");
		user.setFullName("Test 123");
		user.setRole(role);

		userService.create(user);

		return user;
	}
}
