package appliances.services;

import java.util.List;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.CategoryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Category;

@Service
public class CategoryService {
	
	private final CategoryDAO categoryDAO;

	public CategoryService() {
		this.categoryDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getCategoryDAO();
	}
	
	public List<Category> getAll() {
		return categoryDAO.getAll();
	}
	
	public Category getById(int id) {
		return categoryDAO.getById(id);
	}
	
	public Category create(Category category) {
		return categoryDAO.create(category);
	}
	
	public void update(Category category) {
		final boolean result = categoryDAO.update(category);
		
		if (!result) {
			throw new AppliancesRequestException("No category has been updated!");
		}
	}
	
	public void delete(int id) {
		final boolean result = categoryDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No category has been deleted!");
		}
	}
	
}