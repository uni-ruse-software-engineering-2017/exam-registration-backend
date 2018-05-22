package uniruse.mse.examregistration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserAlreadyActivatedException extends RuntimeException {
	private static final long serialVersionUID = -6863845493822721280L;

	public UserAlreadyActivatedException(String message) {
		super(message);
	}
}
