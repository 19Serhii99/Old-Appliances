package appliances.models;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.lang.Nullable;

public abstract class Person extends Entity {
	
	@NotEmpty(message = "Last name must be present!")
	@Size(min = 2, max = 50, message = "Last name must have 2 to 50 characters!")
	private String lastName;
	
	@NotEmpty(message = "First name must be present!")
	@Size(min = 2, max = 50, message = "First name must have 2 to 50 characters!")
	private String firstName;
	
	@Nullable
	@Size(min = 2, max = 50, message = "Middle name must have 2 to 50 characters!")
	private String middleName;
	
	@NotEmpty(message = "Phone number must be present!")
	@Pattern(regexp = "^[[+]?[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*]{10,15}$", message = "Invalid phone number!")
	private String phone;
	
	@NotEmpty(message = "Email must be present!")
	@Email(message = "Invalid email!")
	private String email;
	
	@NotEmpty(message = "Password must be present!")
	@Size(min = 5, max = 50, message = "Password must contain 5 to 50 characters!")
	private String password;
	
	public Person() {};
	
	public Person(int id) {
		super(id);
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}