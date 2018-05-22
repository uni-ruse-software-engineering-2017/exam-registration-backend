package uniruse.mse.examregistration.user;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.Professor;
import uniruse.mse.examregistration.user.model.Student;

@RestController
public class UserResource {

	@Autowired
	private UserService userService;

	/**
	 * POST /professors
	 *
	 * Administration end point for adding professors into the system.
	 *
	 * Required role: ADMIN
	 *
	 * @param professor
	 * @return 201 (Created) - The created entity
	 */
	@RequestMapping(method = POST, path = "/professors")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public Professor create(@RequestBody Professor professor) {
		final Professor createdProfessor = userService.createProfessor(professor);
		createdProfessor.setPassword(null);
		return createdProfessor;
	}

	/**
	 * GET /professors
	 *
	 * Administration end point for listing professors
	 *
	 * Required role: ADMIN
	 *
	 * @param professor
	 * @return 200 (OK) - List of professors
	 */
	@RequestMapping(method = GET, path = "/professors")
	@ResponseBody
	public List<Professor> getProfessors() {
		// TODO: Add pagination
		return userService.getProfessors();
	}

	/**
	 * GET /students
	 *
	 * Administration end point for listing students
	 *
	 * Required role: ADMIN
	 *
	 * @return 200 (OK) - List of students
	 */
	@RequestMapping(method = GET, path = "/students")
	@ResponseBody
	public List<Student> getStudents() {
		// TODO: Add pagination
		return userService.getStudents();
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
		final ApplicationUser appUser = userService.getByUsername(auth.getName())
			.orElseThrow(() -> new UsernameNotFoundException(
				"User with username " + auth.getName() + " was not found."
			));


		appUser.setPassword(null);
		return appUser;
	}
}
