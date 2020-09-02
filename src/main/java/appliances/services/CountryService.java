package appliances.services;

import java.util.List;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.CountryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Country;

@Service
public class CountryService {
	
	private final CountryDAO countryDAO;

	public CountryService() {
		this.countryDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getCountryDAO();
	}
	
	public List<Country> getAll() {
		return countryDAO.getAll();
	}
	
	public Country create(Country country) {
		return countryDAO.create(country);
	}
	
	public Country getById(int id) {
		return countryDAO.getById(id);
	}
	
	public void update(int id, String name) {
		final boolean result = countryDAO.updateCountry(id, name);
		
		if (!result) {
			throw new AppliancesRequestException("No country has been updated!");
		}
	}
	
	public void delete(int id) {
		final boolean result = countryDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No country has been deleted!");
		}
	}
}