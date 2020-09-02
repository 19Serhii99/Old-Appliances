package appliances.models;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.Nullable;

@Document(collection = "product")
public class Product extends Entity {
	
	@NotEmpty(message = "Last name must be present!")
	@Size(min = 2, max = 50, message = "Last name must have 2 to 50 characters!")
	private String name;
	
	@Min(value = 0, message = "Price must be equal to or greater than zero!")
	private float price;
	
	@Min(value = 0, message = "Amount must be equal to or greater than zero!")
	private int amount;
	
	private boolean hidden;
	
	@Nullable
	@Min(value = 0, message = "Width must be equal to or greater than zero!")
	private Float width;
	
	@Nullable
	@Min(value = 0, message = "Height must be equal to or greater than zero!")
	private Float height;
	
	@Nullable
	@Min(value = 0, message = "Depth must be equal to or greater than zero!")
	private Float depth;
	
	@Nullable
	@Min(value = 0, message = "Weight must be equal to or greater than zero!")
	private Float weight;
	
	@NotNull(message = "Category must be present!")
	private Category category;
	
	@NotNull(message = "Brand must be present!")
	private Brand brand;

	public Product() {};
	
	public Product(int id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Float getDepth() {
		return depth;
	}

	public void setDepth(Float depth) {
		this.depth = depth;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}
	
}