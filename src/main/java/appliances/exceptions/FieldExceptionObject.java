package appliances.exceptions;

public class FieldExceptionObject {
	
	private final String message;
	private final FieldErrorType fieldError;
	
	public FieldExceptionObject(String message, FieldErrorType fieldError) {
		this.message = message;
		this.fieldError = fieldError;
	}

	public String getMessage() {
		return message;
	}

	public FieldErrorType getFieldError() {
		return fieldError;
	}
	
}