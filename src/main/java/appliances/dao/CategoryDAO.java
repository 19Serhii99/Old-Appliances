package appliances.dao;

import java.util.List;

import appliances.models.Category;

public interface CategoryDAO {
	
	List<Category> getAll();
	Category getById(int id);
	
	Category create(Category category);
	boolean exists(Category category);
	boolean update(Category category);
	boolean delete(int id);
	
}