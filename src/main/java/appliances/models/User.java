package appliances.models;

import java.util.Date;

import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

@Document(collection = "user")
public class User extends Person {
	
	@Nullable
	@Size(min = 2, max = 50, message = "City must have 2 to 50 characters!")
	private String city;
	
	@Nullable
	private Date birthday;
	
	public User() {};
	
	public User(int id) {
		super(id);
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
}