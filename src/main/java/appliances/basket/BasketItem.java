package appliances.basket;

import appliances.models.Product;

public class BasketItem {
	
	private Product product;
	private int amount;
	
	public Product getProduct() {
		return product;
	}
	
	public void setProduct(Product product, boolean increase) {
		this.product = product;
		if (increase) increase();
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void increase() {
		amount++;
	}
	
	public void decrease() {
		amount--;
	}
	
}