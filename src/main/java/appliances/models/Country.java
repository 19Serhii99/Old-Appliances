package appliances.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "country")
public class Country extends Entity {
	
	@NotEmpty(message = "Country name must be present!")
	@Size(min = 2, max = 50, message = "Country name must have 2 to 50 characters!")
	private String name;
	
	public Country() {}
	
	public Country(int id) {
		super(id);
	}
	
	public Country(int id, String name) {
		super(id);
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}