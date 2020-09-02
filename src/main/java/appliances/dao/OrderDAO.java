package appliances.dao;

import java.util.List;
import java.util.Map;

import appliances.models.Order;

public interface OrderDAO {
	
	int create(Order order);
	boolean changeStatus(Order order);
	boolean delete(int id);
	
	List<Order> getAll();
	List<Order> getAllByFilter(Map<String, List<String>> filter, int id);
	Order getById(int id);
	
}