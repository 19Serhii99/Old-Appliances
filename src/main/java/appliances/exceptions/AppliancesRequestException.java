package appliances.exceptions;

public class AppliancesRequestException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public AppliancesRequestException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public AppliancesRequestException(String message) {
		super(message);
	}
}