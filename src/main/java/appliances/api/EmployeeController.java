package appliances.api;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Employee;
import appliances.services.EmployeeService;

@RequestMapping("api/v1/employees")
@RestController
public class EmployeeController {
	
	private final EmployeeService employeeService;
	
	@Autowired
	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}
	
	@GetMapping(path = "{id}")
	public Employee getEmployeeById(@PathVariable int id) {
		return employeeService.getById(id);
	}
	
	@GetMapping(path = "/current")
	public Employee getCurrentEmployee(HttpSession session) {
		final Object object = session.getAttribute("employeeId");
		checkCurrentEmployee(object);
		
		return employeeService.getById((int) object);
	}
	
	private void checkCurrentEmployee(Object object) {
		if (object == null) {
			throw new AppliancesRequestException("Current employee does not exists!");
		}
	}
	
	@PostMapping
	public Employee createEmployee(@Valid @RequestBody Employee employee, BindingResult br) {
		if (br.hasErrors()) generateException(br, true);
		
		return employeeService.create(employee);
	}
	
	private void generateException(BindingResult br, boolean considerAdditional) {
		final List<FieldError> fieldErrors = br.getFieldErrors();
		final List<FieldExceptionObject> errors = new LinkedList<>();
		
		fieldErrors.forEach(fieldError -> {
			FieldErrorType fieldErrorType = FieldErrorType.NONE;
			
			switch (fieldError.getField()) {
				case "lastName":
					fieldErrorType = FieldErrorType.LAST_NAME;
					break;
				case "firstName":
					fieldErrorType = FieldErrorType.FIRST_NAME;
					break;
				case "middleName":
					fieldErrorType = FieldErrorType.MIDDLE_NAME;
					break;
				case "phone":
					fieldErrorType = FieldErrorType.PHONE;
					break;
				case "email":
					fieldErrorType = FieldErrorType.EMAIL;
					break;
				case "password":
					fieldErrorType = FieldErrorType.PASSWORD;
					break;
				case "startDate":
					fieldErrorType = FieldErrorType.DATE;
					break;
				case "position":
					fieldErrorType = FieldErrorType.POSITION;
					break;
			}
			
			if (!((fieldErrorType == FieldErrorType.PASSWORD || fieldErrorType == FieldErrorType.DATE || fieldErrorType == FieldErrorType.POSITION)
					&& !considerAdditional)) {
				final String message = fieldError.getDefaultMessage();
				final FieldExceptionObject object = new FieldExceptionObject(message, fieldErrorType);
				
				errors.add(object);
			}
		});
		
		if (!errors.isEmpty()) {
			throw new FormRequestException(errors);
		}
	}
	
	@PostMapping(path = "sign-in")
	public void signIn(@RequestBody ObjectNode json, HttpSession session) {
		final Employee employee = employeeService.signIn(json.get("email").asText(), json.get("password").asText());
		session.setAttribute("employeeId", employee.getId());
	}
	
	@GetMapping(path = "has-authorized")
	public String hasAuthorized(HttpSession session) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		final boolean hasAuthorized = session.getAttribute("employeeId") != null;
		
		return mapper.writeValueAsString(hasAuthorized);
	}
	
	@PutMapping(path = "sign-out")
	public void signOut(HttpSession session) {
		session.setAttribute("employeeId", null);
	}
	
	@PostMapping(path = "update")
	public void updateEmployee(@RequestBody @Valid Employee employee, BindingResult br, HttpSession session) {
		if (br.hasErrors()) generateException(br, false);
		
		final Integer id = (int) session.getAttribute("employeeId");
		employee.setId(id);
		
		employeeService.update(employee);
	}
	
	@PutMapping(path = "update-password/{password}")
	public void updatePassword(@PathVariable String password, HttpSession session) {
		password = password.trim();
		
		if (password.length() < 5 || password.length() > 50) {
			throw new AppliancesRequestException("Password must contain 5 to 50 characters!");
		}
		
		final Integer id = (int) session.getAttribute("employeeId");
		employeeService.updatePassword(id, password);
	}
	
	@DeleteMapping(path = "{id}")
	public void deleteEmployee(@PathVariable int id) {
		employeeService.delete(id);
	}
}