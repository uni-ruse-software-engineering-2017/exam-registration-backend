package uniruse.mse.examregistration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.h2.util.StringUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import uniruse.mse.examregistration.user.UserService;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.LoginUser;
import uniruse.mse.examregistration.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = { ExamRegistrationBackendApplication.class, H2Config.class })
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

	protected MediaType jsonContent = new MediaType(
		MediaType.APPLICATION_JSON.getType(),
		MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8")
	);

	protected String fromFile(String path) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(path));
		} catch (final IOException e) {
			throw new IllegalArgumentException();
		}
	}

	@Before
	@Transactional
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(springSecurity())
			.build();
	}

	/**
	 * Helper method for dispatching HTTP GET requests
	 * with application/json response type.
	 *
	 * @param url
	 *            - end point URI
	 * @param jwt
	 * 			  - JSON Web Token used for authentication
	 * @return HTTP application/json response
	 * @throws Exception
	 */
	protected ResultActions get(String url, String jwt) throws Exception {
		final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON);

		if (!StringUtils.isNullOrEmpty(jwt)) {
			builder.header("Authorization", "Bearer " + jwt);
		}

		return this.mockMvc.perform(builder);
	}


	/**
	 * Helper method for dispatching HTTP POST requests with application/json
	 * content and response type.
	 *
	 * @param url
	 *            - end point URI
	 * @param jsonBody
	 *            - HTTP JSON body
	 * @param jwt
	 * 			  - JSON Web Token used for authentication
	 * @return HTTP application/json response
	 * @throws Exception
	 */
	protected ResultActions post(String url, String jsonBody, String jwt) throws Exception {
		return this.mockMvc.perform(MockMvcRequestBuilders.post(url)
			.header("Authorization", jwt != "" ? "Bearer " + jwt : "")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonBody)
		);
	}

	/**
	 * Helper method for dispatching HTTP PATCH requests with application/json
	 * content and response type.
	 *
	 * @param url
	 *            - end point URI
	 * @param jsonBody
	 *            - HTTP JSON body
	 * @param jwt
	 * 			  - JSON Web Token used for authentication
	 * @return HTTP application/json response
	 * @throws Exception
	 */
	protected ResultActions patch(String url, String jsonBody, String jwt)
			throws Exception {

		return this.mockMvc.perform(MockMvcRequestBuilders.patch(url)
			.header("Authorization", jwt != "" ? "Bearer " + jwt : "")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonBody));
	}

	protected String toJson(Object object) {
		try {
			final ObjectMapper mapper = new ObjectMapper();

			// ignore annotations - include all fields even if ignored
			mapper.disable(MapperFeature.USE_ANNOTATIONS);

			return mapper.writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected ApplicationUser createUser(String name, String password, UserRole role) {
		final ApplicationUser user = new ApplicationUser();
		user.setUsername(name);
		user.setPassword(password);
		user.setFullName("Test 123");
		user.setRole(role);

		return userService.create(user);
	}

	protected ApplicationUser createActiveUser(String name, String password, UserRole role) {
		final ApplicationUser user = createUser(name, password, role);

		final String token = userService.generateActicationToken(name);

		userService.activate(name, token);

		return user;
	}

	/**
	 * Utility method for authenticating with an account with the student role.
	 *
	 * @return Pair of: (User object, JWT token)
	 * @throws Exception
	 */
	protected Pair<ApplicationUser, String> loginAsStudent() throws Exception {
		final String username = "s136500@stud.uni-ruse.bg";
		final String password = "12345678";
		final ApplicationUser user = createActiveUser(username, password, UserRole.STUDENT);

		final LoginUser credentials = new LoginUser() {{
			setUsername(username);
			setPassword(password);
		}};

		final String jwt = this.login(credentials);

		return Pair.of(user, jwt);
	}

	/**
	 * Utility method for authenticating with an account with the administrator role.
	 *
	 * @return Pair of: (User object, JWT token)
	 * @throws Exception
	 */
	protected Pair<ApplicationUser, String> loginAsAdmin() throws Exception {
		final String username = "admin@exams.uni-ruse.bg";
		final String password = "12345678";
		final ApplicationUser user = createActiveUser(username, password, UserRole.ADMIN);

		final LoginUser credentials = new LoginUser() {{
			setUsername(username);
			setPassword(password);
		}};

		final String jwt = this.login(credentials);

		return Pair.of(user, jwt);
	}

	/**
	 * Performs a login request.
	 *
	 * @param credentials - user credentials to authenticate with
	 * @return JWT token
	 * @throws Exception
	 */
	protected String login(LoginUser credentials) throws Exception {
		final String jsonBody = toJson(credentials);

		final MockHttpServletResponse response = this.post("/login", jsonBody, "")
			.andReturn()
			.getResponse();

		if (response.getStatus() != 200) {
			throw new IllegalArgumentException("Login failed");
		}

		// convert JSON string to HashMap<String, String>
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, String> jsonResult = new HashMap<>();
		jsonResult = mapper.readValue(
			response.getContentAsString(),
			new TypeReference<Map<String, String>>(){}
		);

		return jsonResult.get("token");
	}
}
