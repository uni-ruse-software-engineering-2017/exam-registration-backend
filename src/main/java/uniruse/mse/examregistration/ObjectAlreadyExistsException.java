package uniruse.mse.examregistration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when there is already an existing object with the same id.
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ObjectAlreadyExistsException extends RuntimeException {

	public ObjectAlreadyExistsException(String message) {
		super(message);
	}

}
