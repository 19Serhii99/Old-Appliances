package appliances.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "basket")
public class Basket {
	
	private Product product;
	
	private float price;
	private int amount;
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
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
	
}