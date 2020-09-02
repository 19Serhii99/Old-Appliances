package appliances.exceptions;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppliancesExceptionHandler {
	
	@ExceptionHandler(value = { AppliancesRequestException.class })
	public ResponseEntity<Object> handleAppliancesException(AppliancesRequestException e) {
		
		final AppliancesException appliancesException = new AppliancesException(
				e.getMessage(), HttpStatus.BAD_REQUEST, ZonedDateTime.now(ZoneId.of("Z")));
		
		return new ResponseEntity<>(appliancesException, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value = { FormRequestException.class })
	public ResponseEntity<Object> handleFormException(FormRequestException e) {
		
		final FormException formException = new FormException(
				e.getMessage(), HttpStatus.BAD_REQUEST, ZonedDateTime.now(ZoneId.of("Z")), e.getErrors());
		
		return new ResponseEntity<>(formException, HttpStatus.BAD_REQUEST);
	}
	
}