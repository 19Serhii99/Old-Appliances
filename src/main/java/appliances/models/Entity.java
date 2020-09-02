package appliances.models;

import org.springframework.data.annotation.Id;

public abstract class Entity {
	
	@Id
	private int id;
	
	public Entity() {};
	
	public Entity(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}