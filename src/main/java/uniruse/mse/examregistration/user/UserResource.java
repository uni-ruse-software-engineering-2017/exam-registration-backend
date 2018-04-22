package uniruse.mse.examregistration.user;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uniruse.mse.examregistration.user.model.ApplicationUser;

@RestController
public class UserResource {

	@Autowired
	private UserService userService;

	/**
	 * POST /users
	 *
	 * Private end point for creating new users.
	 *
	 * Required role: ADMIN
	 *
	 * @param user
	 * @return 201 (Created) - The created entity
	 */
	@RequestMapping(method = POST, path = "/users")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.CREATED)
	public ApplicationUser create(@RequestBody ApplicationUser user) {
		final ApplicationUser createdUser = userService.create(user);

		return createdUser;
	}


	/**
	 * GET /profile
	 *
	 * Gets detailed information for the current user.
	 *
	 * @param auth - the authenticated user
	 * @return Status: 200 (OK) | User Profile
	 */
	@RequestMapping(method = GET, path = "/profile")
	@ResponseBody()
	public ApplicationUser login(Authentication auth) {
		final Optional<ApplicationUser> appUser = userService.getByUsername(auth.getName());

		if (!appUser.isPresent()) {
			throw new UsernameNotFoundException("User with username " + auth.getName() + " was not found.");
		}

		return appUser.get();
	}
}
