package uniruse.mse.examregistration.user;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/users")
public class UserResource {

	@Autowired
	private UserService userService;

	@RequestMapping(method = POST)
	public ResponseEntity<?> create(@RequestBody ApplicationUser user) {
		userService.create(user);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

}
