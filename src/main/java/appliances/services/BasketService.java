package appliances.services;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import appliances.basket.BasketItem;
import appliances.basket.SessionBasket;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Product;

@Service
public class BasketService {
	
	private final ProductService productService;
	
	@Autowired
	public BasketService(ProductService productService) {
		this.productService = productService;
	}
	
	public void addProduct(SessionBasket basket, int productId) {
		BasketItem item = getBasketItem(basket, productId);
		
		final Product product = productService.getById(productId);
		checkProduct(product);
		
		if (item == null) {
			item = new BasketItem();
			item.setProduct(product, true);
			basket.getItems().add(item);
		} else {
			checkProductAmount(item, product);
			item.increase();
		}
		
		basket.setSum(basket.getSum() + product.getPrice());
	}
	
	private void checkProduct(Product product) {
		if (product == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
		
		if (product.getAmount() == 0) {
			throw new AppliancesRequestException("This product is unavailable!");
		}
	}
	
	private void checkProductAmount(BasketItem item, Product product) {
		if (item.getAmount() >= product.getAmount()) {
			throw new AppliancesRequestException("Not enough products! You can order only " + product.getAmount() + " such products");
		}
	}
	
	public void decreaseProductAmount(SessionBasket basket, int productId) {
		final BasketItem item = getBasketItem(basket, productId);
		
		checkBasketItem(item);
		decreaseValues(basket, item);
		checkBasketItemAmount(basket, item);
	}
	
	private void checkBasketItem(BasketItem item) {
		if (item == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
	}
	
	private void decreaseValues(SessionBasket basket, BasketItem item) {
		item.decrease();
		basket.setSum(basket.getSum() - item.getProduct().getPrice());
	}
	
	private void checkBasketItemAmount(SessionBasket basket, BasketItem item) {
		if (item.getAmount() == 0) {
			basket.getItems().remove(item);
		}
	}
	
	public void dropProduct(SessionBasket basket, int productId) {
		final BasketItem item = getBasketItem(basket, productId);
		
		if (item == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
		
		basket.setSum(basket.getSum() - item.getAmount() * item.getProduct().getPrice());
		basket.getItems().remove(item);
	}
	
	private BasketItem getBasketItem(SessionBasket basket, int productId) {
		final List<BasketItem> list = basket.getItems()
				.stream()
				.filter(i -> i.getProduct().getId() == productId)
				.limit(1)
				.collect(Collectors.toList());
		
		return list.isEmpty() ? null : list.get(0);
	}
	
	public SessionBasket updateBasket(SessionBasket basket) {
		final List<BasketItem> removedItems = new LinkedList<>();
		
		basket.getItems().forEach(item -> {
			final Product product = productService.getById(item.getProduct().getId());
			
			if (product == null) {
				removedItems.add(item);
			} else {
				item.setProduct(product, false);
			}
		});
	
		removedItems.forEach(item -> basket.getItems().remove(item));
		
		basket.setSum(0.0f);
		basket.getItems().forEach(item -> basket.setSum(basket.getSum() + item.getAmount() * item.getProduct().getPrice()));
		
		return basket;
	}
	
}