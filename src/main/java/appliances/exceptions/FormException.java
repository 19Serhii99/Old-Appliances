package appliances.exceptions;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

class FormException extends AppliancesException {
	
	private final List<FieldExceptionObject> errors;
	
	public FormException(String message, HttpStatus httpStatus, ZonedDateTime timestamp, List<FieldExceptionObject> errors) {
		super(message, httpStatus, timestamp);
		this.errors = errors;
	}

	public List<FieldExceptionObject> getErrors() {
		return errors;
	}
	
}