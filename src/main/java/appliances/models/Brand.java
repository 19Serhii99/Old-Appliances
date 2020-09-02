package appliances.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "brand")
public class Brand extends Entity {
	
	@NotEmpty(message = "Brand name must be present!")
	@Size(min = 2, max = 50, message = "Brand name must have 2 to 50 characters!")
	private String name;
	
	@NotNull(message = "Country must be present!")
	private Country country;
	
	public Brand() {
		super();
	}
	
	public Brand(int id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
}