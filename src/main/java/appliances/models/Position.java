package appliances.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "position")
public class Position extends Entity {
	
	@NotEmpty(message = "Position name must be present!")
	@Size(min = 2, max = 50, message = "Position name must have 2 to 50 characters!")
	private String name;
	
	public Position() {}

	public Position(int id) {
		super(id);
	}
	
	public Position(int id, String name) {
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