package appliances.basket;

import java.util.LinkedList;
import java.util.List;

public class SessionBasket {
	
	private List<BasketItem> items;
	
	private float sum;
	
	public SessionBasket() {
		items = new LinkedList<>();
	}
	
	public List<BasketItem> getItems() {
		return items;
	}

	public float getSum() {
		return sum;
	}

	public void setSum(float sum) {
		this.sum = sum;
	}
	
}