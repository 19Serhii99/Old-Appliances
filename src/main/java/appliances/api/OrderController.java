package appliances.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import appliances.basket.SessionBasket;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Order;
import appliances.models.User;
import appliances.services.OrderService;

@RequestMapping("api/v1/orders")
@RestController
public class OrderController {
	
	private final OrderService orderService;

	@Autowired
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@PostMapping
	public String makeOrder(@Valid @RequestBody Order order, BindingResult br, HttpSession session) throws JsonProcessingException {
		if (br.hasErrors()) generateException(br);
		
		final Object userId = session.getAttribute("userId");
		if (userId != null) {
			final User user = new User((int) userId);
			order.setUser(user);
		}
		
		final SessionBasket sessionBasket = (SessionBasket) session.getAttribute("basket");
		final Integer orderId = orderService.makeOrder(sessionBasket, order);
		final ObjectMapper mapper = new ObjectMapper();
		
		session.setAttribute("basket", null);
		
		return mapper.writeValueAsString(orderId);
	}
	
	private void generateException(BindingResult br) {
		final List<FieldError> errors = br.getFieldErrors();
		final List<FieldExceptionObject> errorList = new LinkedList<>();
		
		errors.forEach(error -> {
			FieldErrorType fieldErrorType = FieldErrorType.NONE;
			
			switch(error.getField()) {
			case "lastName":
				fieldErrorType = FieldErrorType.LAST_NAME;
				break;
			case "firstName":
				fieldErrorType = FieldErrorType.FIRST_NAME;
				break;
			case "middleName":
				fieldErrorType = FieldErrorType.MIDDLE_NAME;
				break;
			case "phone":
				fieldErrorType = FieldErrorType.PHONE;
				break;
			case "city":
				fieldErrorType = FieldErrorType.CITY;
				break;
			}
			
			final FieldExceptionObject object = new FieldExceptionObject(error.getDefaultMessage(), fieldErrorType);
			errorList.add(object);
		});
		
		throw new FormRequestException(errorList);
	}
	
	@GetMapping
	public List<Order> getAllOrders() {
		return orderService.getAll();
	}
	
	@GetMapping(path = "{filter}")
	public List<Order> getAllOrders(@MatrixVariable Map<String, List<String>> filter, HttpSession session) {
		final Object object = session.getAttribute("employeeId");
		return orderService.getAllByFilter(filter, (int) object);
	}
	
	@PutMapping(path = "{id}")
	public void changeStatus(@PathVariable int id, HttpSession session) {
		final Object object = session.getAttribute("employeeId");
		orderService.changeStatus(id, (int) object);
	}
	
	@DeleteMapping(path = "{id}")
	public void delete(@PathVariable int id) {
		orderService.delete(id);
	}
}