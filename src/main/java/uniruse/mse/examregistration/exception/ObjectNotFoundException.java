package uniruse.mse.examregistration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the requested object is not found in the system.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {

	public ObjectNotFoundException(String message) {
		super(message);
	}
}
