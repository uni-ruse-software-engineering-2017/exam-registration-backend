package uniruse.mse.examregistration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the requested operation is not allowed.
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class OperationNotAllowedException extends RuntimeException {

	public OperationNotAllowedException(String message) {
		super(message);
	}
}
