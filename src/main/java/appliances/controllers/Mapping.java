package appliances.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Mapping {
	
	@GetMapping("/")
	public String homePage() {
		return "index";
	}
	
	@GetMapping("navbar")
	public String getNavbar() {
		return "navbar";
	}
	
	@GetMapping("login")
	public String getLogin() {
		return "sign-in";
	}
	
	@GetMapping("register")
	public String getRegister() {
		return "sign-up";
	}
	
	@GetMapping("products")
	public String getProducts() {
		return "products";
	}
	
	@GetMapping("add-product")
	public String getAddProduct() {
		return "add-product";
	}
	
	@GetMapping("countries")
	public String getCountries() {
		return "countries";
	}
	
	@GetMapping("brands")
	public String getBrands() {
		return "brands";
	}
	
	@GetMapping("error")
	public String getErrorPage() {
		return "error";
	}
	
	@GetMapping("basket")
	public String getBasket() {
		return "basket";
	}
	
	@GetMapping("make-order")
	public String getMakeOrder() {
		return "make-order";
	}
	
	@GetMapping("product")
	public String getProduct() {
		return "product";
	}
	
	@GetMapping("account")
	public String getAccountPage() {
		return "account";
	}
	
	@GetMapping("orders")
	public String getOrders() {
		return "orders";
	}
	
}