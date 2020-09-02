package appliances.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.ProductDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Product;

@Service
public class ProductService {
	
	private final ProductDAO productDAO;

	public ProductService() {
		this.productDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getProductDAO();
	}
	
	public List<Product> getAll(int categoryId) {
		return productDAO.getAll(categoryId);
	}
	
	public List<Product> getAllByFilter(int categoryId, Map<String, List<String>> filter) {
		return productDAO.getAllByFilter(categoryId, filter);
	}
	
	public List<Product> search(String text) {
		return productDAO.search(text);
	}
	
	public Product create(Product product) {
		return productDAO.create(product);
	}
	
	public float getMinPrice(int categoryId, Map<String, List<String>> filter) {
		return productDAO.getMinPrice(categoryId, filter);
	}
	
	public float getMaxPrice(int categoryId, Map<String, List<String>> filter) {
		return productDAO.getMaxPrice(categoryId, filter);
	}
	
	public Product getById(int productId) {
		return productDAO.getById(productId);
	}
	
	public void update(Product product) {
		final boolean result = productDAO.update(product);
		
		if (!result) {
			throw new AppliancesRequestException("No product has been updated!");
		}
	}
	
	public void hide(int id) {
		final boolean result = productDAO.hide(id);
		
		if (!result) {
			throw new AppliancesRequestException("No product has been hidden!");
		}
	}
	
	public void show(int id) {
		final boolean result = productDAO.show(id);
		
		if (!result) {
			throw new AppliancesRequestException("No product has been shown!");
		}
	}
	
	public void delete(int id) {
		final boolean result = productDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No product has been deleted!");
		}
	}
	
	public void fillProducts() {
		productDAO.fillRandomData();
	}
	
}