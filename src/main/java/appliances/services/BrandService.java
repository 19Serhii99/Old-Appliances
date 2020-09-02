package appliances.services;

import java.util.List;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.BrandDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Brand;

@Service
public class BrandService {
	
	private final BrandDAO brandDAO;
	
	public BrandService() {
		this.brandDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getBrandDAO();
	}
	
	public List<Brand> getAll() {
		return brandDAO.getAll();
	}
	
	public List<Brand> getAllByCategory(int categoryId) {
		return brandDAO.getAllByCategory(categoryId);
	}
	
	public Brand getById(int id) {
		return brandDAO.getById(id);
	}
	
	public Brand create(Brand brand) {
		return brandDAO.create(brand);
	}
	
	public void update(Brand brand) {
		final boolean result = brandDAO.update(brand);
		
		if (!result) {
			throw new AppliancesRequestException("No brand has been updated!");
		}
	}
	
	public void delete(int id) {
		final boolean result = brandDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No brand has been deleted!");
		}
	}
	
}