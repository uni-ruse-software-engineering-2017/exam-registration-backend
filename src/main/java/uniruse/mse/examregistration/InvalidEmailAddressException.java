package uniruse.mse.examregistration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the provided email address is not a valid student's or
 * professor's email address belonging to the University of Ruse domain.
 * 
 * Valid email addresses:
 * 
 * Student: s136510@stud.uni-ruse.bg
 * Professor: p.name@ami.uni-ruse.bg
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidEmailAddressException extends RuntimeException {
	private static final long serialVersionUID = -7363812223885196079L;

	public InvalidEmailAddressException(String message) {
		super(message);
	}
}
