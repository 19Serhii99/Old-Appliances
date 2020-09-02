package appliances.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "status")
public class Status extends Entity {
	private String name;
	
	public Status() {}

	public Status(int id) {
		super(id);
	}
	
	public Status(int id, String name) {
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