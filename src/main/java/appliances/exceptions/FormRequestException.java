package appliances.exceptions;

import java.util.LinkedList;
import java.util.List;

public class FormRequestException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private final List<FieldExceptionObject> errors;
	
	public FormRequestException(FieldExceptionObject fieldExceptionObject) {
		errors = new LinkedList<>();
		errors.add(fieldExceptionObject);
	}
	
	public FormRequestException(List<FieldExceptionObject> errors) {
		this.errors = errors;
	}
	
	public FormRequestException(List<FieldExceptionObject> errors, Throwable cause) {
		super(cause);
		this.errors = errors;
	}

	public List<FieldExceptionObject> getErrors() {
		return errors;
	}
	
}