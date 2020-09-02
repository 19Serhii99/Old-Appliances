package appliances.dao;

import java.util.List;

import appliances.models.Country;

public interface CountryDAO {
	
	List<Country> getAll();
	
	Country getById(int id);
	Country create(Country country);
	
	boolean exists(Country country);
	boolean updateCountry(int id, String name);
	boolean delete(int id);
	
}