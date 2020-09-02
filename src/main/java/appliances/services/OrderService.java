package appliances.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import appliances.basket.BasketItem;
import appliances.basket.SessionBasket;
import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.OrderDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Basket;
import appliances.models.Employee;
import appliances.models.Order;
import appliances.models.Product;
import appliances.models.Status;
import appliances.models.User;

@Service
public class OrderService {
	
	private final UserService userService;
	private final ProductService productService;
	private final StatusService statusService;
	private final EmployeeService employeeService;
	
	private final OrderDAO orderDAO;

	@Autowired
	public OrderService(UserService userService, ProductService productService, StatusService statusService, EmployeeService employeeService) {
		this.userService = userService;
		this.productService = productService;
		
		this.statusService = statusService;
		this.employeeService = employeeService;
		
		this.orderDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getOrderDAO();
	}
	
	@Transactional
	public int makeOrder(SessionBasket basket, Order order) {
		checkUser(order);
		checkBasket(basket);
		
		final List<Basket> baskets = new LinkedList<>();
		basket.getItems().forEach(item -> baskets.add(handleSessionBasketItem(item)));
		
		return createOrder(order, baskets, basket.getSum());
	}
	
	private void checkUser(Order order) {
		if (order.getUser() == null) return;
		
		final User user = userService.getById(order.getUser().getId());
		
		if (user == null) {
			throw new AppliancesRequestException("User with the specified id does not exist!");
		}
	}
	
	private void checkBasket(SessionBasket basket) {
		if (basket == null) {
			throw new AppliancesRequestException("There is no any item in the shopping cart!");
		}
	}
	
	private int createOrder(Order order, List<Basket> baskets, float sum) {
		final Status status = statusService.getByName("New");
		
		order.setStatus(status);
		order.setBaskets(baskets);
		order.setDeliveryCost(50f);
		order.setTotal(sum);
		
		return orderDAO.create(order);
	}
	
	private Product computeProduct(BasketItem item) {
		final Product product = productService.getById(item.getProduct().getId());
		checkProductAmount(product.getAmount(), item.getAmount());
		
		final int amount = product.getAmount() - item.getAmount();
		product.setAmount(amount);
		
		productService.update(product);
		return product;
	}
	
	private void checkProductAmount(int productAmount, int orderedAmount) {
		if (productAmount < orderedAmount) {
			throw new AppliancesRequestException("Not enough product amount!");
		}
	}
	
	private Basket fillBasketData(Product product, int amount) {
		final Basket basket = new Basket();
		
		basket.setAmount(amount);
		basket.setPrice(product.getPrice());
		basket.setProduct(product);
		
		return basket;
	}
	
	private Basket handleSessionBasketItem(BasketItem item) {
		final Product product = computeProduct(item);
		return fillBasketData(product, item.getAmount());
	}
	
	public List<Order> getAll() {
		return orderDAO.getAll();
	}
	
	public List<Order> getAllByFilter(Map<String, List<String>> filter, int id) {
		return orderDAO.getAllByFilter(filter, id);
	}
	
	public void changeStatus(int idOrder, int idEmployee) {
		final Order order = orderDAO.getById(idOrder);
		final Status currentStatus = order.getStatus();
		
		Status newStatus = null;
		
		if ("New".equals(currentStatus.getName())) {
			newStatus = statusService.getByName("Being processed");
			
			final Employee employee = employeeService.getById(idEmployee);
			order.setEmployee(employee);
			
		} else if ("Being processed".equals(currentStatus.getName())) {
			newStatus = statusService.getByName("Completed");
		}
		
		if (newStatus == null) {
			throw new AppliancesRequestException("Could not change status code!");
		}
		
		order.setStatus(newStatus);
		orderDAO.changeStatus(order);
	}
	
	@Transactional
	public void delete(int id) {
		final Order order = orderDAO.getById(id);
		
		order.getBaskets()
			.forEach(basket -> {
				final Product product = productService.getById(basket.getProduct().getId());
				final int amount = product.getAmount() + basket.getAmount();
				
				product.setAmount(amount);
				productService.update(product);
			});
		
		orderDAO.delete(id);
	}
	
}