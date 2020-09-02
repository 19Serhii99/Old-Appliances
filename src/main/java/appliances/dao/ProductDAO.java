package appliances.dao;

import java.util.List;
import java.util.Map;

import appliances.models.Product;

public interface ProductDAO {
	
	List<Product> getAll(int categoryId);
	List<Product> getAllByFilter(int categoryId, Map<String, List<String>> filter);
	List<Product> search(String text);
	
	Product getById(int productId);
	Product create(Product product);
	
	boolean exists(Product product);
	boolean update(Product product);
	boolean show(int id);
	boolean hide(int id);
	boolean delete(int id);
	
	float getMinPrice(int categoryId, Map<String, List<String>> filter);
	float getMaxPrice(int categoryId, Map<String, List<String>> filter);
	
	void fillRandomData();
	
}