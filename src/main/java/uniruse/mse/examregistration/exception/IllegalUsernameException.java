package uniruse.mse.examregistration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class IllegalUsernameException extends RuntimeException {
	private static final long serialVersionUID = -6863845493822321280L;

	public IllegalUsernameException(String message) {
		super(message);
	}
}
