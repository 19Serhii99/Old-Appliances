package appliances.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.Nullable;

@Document(collection = "category")
public class Category extends Entity {
	
	@NotEmpty(message = "Category name must be present!")
	@Size(min = 2, max = 50, message = "Category must have 2 to 50 characters!")
	private String name;
	
	@Nullable
	private Category parent;
	
	public Category() {};
	
	public Category(int id) {
		super(id);
	}
	
	public Category(int id, String name) {
		super(id);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Category getParent() {
		return parent;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}
	
}