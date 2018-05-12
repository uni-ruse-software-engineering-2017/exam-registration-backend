package uniruse.mse.examregistration.user;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uniruse.mse.examregistration.user.model.SignUpUser;

@RestController()
public class AuthenticationResource {
	@Autowired
	private UserService userService;

	/**
	 * POST /sign-up
	 *
	 * Public end point for new users to sign up into the system.
	 *
	 * @param user
	 * @return 201 (Created)
	 */
	@RequestMapping(method = POST, path = "/sign-up")
	public ResponseEntity<?> signUp(@RequestBody SignUpUser user) {
		userService.create(user);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@RequestMapping(method = GET, path = "/activate/{username}")
	public void activate(@PathVariable("username") String username, @RequestParam("token") String token) {
		userService.activate(username, token);
	}

}
