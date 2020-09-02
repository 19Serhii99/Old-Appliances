package appliances.config;

import appliances.dao.BrandDAO;
import appliances.dao.CategoryDAO;
import appliances.dao.CountryDAO;
import appliances.dao.EmployeeDAO;
import appliances.dao.OrderDAO;
import appliances.dao.PositionDAO;
import appliances.dao.ProductDAO;
import appliances.dao.StatusDAO;
import appliances.dao.UserDAO;

public interface IFactory {
	
	BrandDAO getBrandDAO();
	CategoryDAO getCategoryDAO();
	CountryDAO getCountryDAO();
	
	EmployeeDAO getEmployeeDAO();
	OrderDAO getOrderDAO();
	PositionDAO getPositionDAO();
	
	ProductDAO getProductDAO();
	StatusDAO getStatusDAO();
	UserDAO getUserDAO();
	
}