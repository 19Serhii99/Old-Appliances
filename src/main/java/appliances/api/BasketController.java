package appliances.api;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import appliances.basket.SessionBasket;
import appliances.exceptions.AppliancesRequestException;
import appliances.services.BasketService;

@RequestMapping("api/v1/basket")
@RestController
public class BasketController {
	
	final BasketService basketService;
	
	@Autowired
	public BasketController(BasketService basketService) {
		this.basketService = basketService;
	}
	
	@GetMapping
	public SessionBasket getBasket(HttpSession session) {
		return getBasketFromSession(session);
	}
	
	@GetMapping(path = "amount")
	public String getBasketAmount(HttpSession session) throws JsonProcessingException {
		final SessionBasket basket = getBasket(session);
		final ObjectMapper mapper = new ObjectMapper();

		if (basket == null) {
			return mapper.writeValueAsString(0);
		}
		
		return mapper.writeValueAsString(basket.getItems().size());
	}
	
	@PostMapping
	public void addProductToBasket(@RequestBody ObjectNode node, HttpSession session) {
		SessionBasket basket = getBasketFromSession(session);
		
		if (basket == null) {
			basket = new SessionBasket();
			session.setAttribute("basket", basket);
		}
		
		final String productText = node.get("productId").asText();
		final int productId = Integer.parseInt(productText);
		
		basketService.addProduct(basket, productId);
	}
	
	@PutMapping(path = "{productId}")
	public void decreaseProductAmount(@PathVariable int productId, HttpSession session) {
		final SessionBasket basket = getBasketFromSession(session);
		checkShoppingCart(basket);
		
		basketService.decreaseProductAmount(basket, productId);
	}
	
	@DeleteMapping(path = "{productId}")
	public void dropProductFromBasket(@PathVariable int productId, HttpSession session) {
		final SessionBasket basket = getBasketFromSession(session);
		checkShoppingCart(basket);
		
		basketService.dropProduct(basket, productId);
	}
	
	private void checkShoppingCart(SessionBasket basket) {
		if (basket == null) {
			throw new AppliancesRequestException("There is no items in shopping cart!");
		}
	}
	
	private SessionBasket getBasketFromSession(HttpSession session) {
		final Object object = session.getAttribute("basket");
		return object == null ? null : basketService.updateBasket((SessionBasket) object);
	}
	
}