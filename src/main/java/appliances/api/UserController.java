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
import appliances.models.User;
import appliances.services.UserService;

@RequestMapping("api/v1/users")
@RestController
public class UserController {
	
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping(path = "{id}")
	public User getUserById(@PathVariable int id) {
		return userService.getById(id);
	}
	
	@GetMapping(path = "/current")
	public User getCurrentUser(HttpSession session) {
		final Object object = session.getAttribute("userId");
		checkCurrentUser(object);
		
		return userService.getById((int) object);
	}
	
	private void checkCurrentUser(Object object) {
		if (object == null) {
			throw new AppliancesRequestException("Current user does not exists!");
		}
	}
	
	@PostMapping
	public void createUser(@Valid @RequestBody User user, BindingResult br) {
		if (br.hasErrors()) generateException(br, true);
		
		userService.create(user);
	}
	
	private void generateException(BindingResult br, boolean considerPassword) {
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
				case "city":
					fieldErrorType = FieldErrorType.CITY;
					break;
			}
			
			if (!(fieldErrorType == FieldErrorType.PASSWORD && !considerPassword)) {
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
		final String email = json.get("email").asText();
		final String password = json.get("password").asText();
		
		final User user = userService.signIn(email, password);
		
		session.setAttribute("userId", user.getId());
	}
	
	@GetMapping(path = "has-authorized")
	public String hasAuthorized(HttpSession session) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		final boolean hasAuthorized = session.getAttribute("userId") != null;
		
		return mapper.writeValueAsString(hasAuthorized);
	}
	
	@PutMapping(path = "sign-out")
	public void signOut(HttpSession session) {
		session.setAttribute("userId", null);
	}
	
	@PostMapping(path = "update")
	public void updateUser(@RequestBody @Valid User user, BindingResult br, HttpSession session) {
		if (br.hasErrors()) generateException(br, false);
		
		final Integer id = (int) session.getAttribute("userId");
		user.setId(id);
		
		userService.update(user);
	}
	
	@PutMapping(path = "update-password/{password}")
	public void updatePassword(@PathVariable String password, HttpSession session) {
		password = password.trim();
		
		if (password.length() < 5 || password.length() > 50) {
			throw new AppliancesRequestException("Password must contain 5 to 50 characters!");
		}
		
		final Integer id = (int) session.getAttribute("userId");
		userService.updatePassword(id, password);
	}
	
	@DeleteMapping(path = "{id}")
	public void deleteUser(@PathVariable int id) {
		userService.delete(id);
	}
	
}