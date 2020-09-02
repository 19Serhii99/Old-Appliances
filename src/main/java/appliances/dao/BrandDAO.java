package appliances.dao;

import java.util.List;

import appliances.models.Brand;

public interface BrandDAO {
	
	List<Brand> getAll();
	List<Brand> getAllByCategory(int categoryId);
	
	Brand getById(int id);
	
	boolean exists(Brand brand);
	boolean update(Brand brand);
	boolean delete(int id);

	Brand create(Brand brand);
	
}