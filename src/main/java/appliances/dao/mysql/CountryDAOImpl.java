package appliances.dao.mysql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import appliances.dao.CountryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Country;

@Transactional
public class CountryDAOImpl implements CountryDAO {
	
	private final JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final static String getAll = "SELECT id, name FROM country";
	
	private final static String getById = getAll + " WHERE id = ? LIMIT 1";
	
	private final static String exists = "SELECT 1 FROM country WHERE EXISTS (SELECT * FROM country WHERE name = ?) LIMIT 1";
	
	private final static String update = "UPDATE country SET name = ? WHERE id = ?";
	
	private final static String delete = "DELETE from country WHERE id = ?";
	
	private final static String countryReferenceExists = "SELECT 1 FROM brand WHERE EXISTS (SELECT * FROM brand WHERE id_country = ?)";
	
	public CountryDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Country> getAll() {
		return jdbcTemplate.query(getAll, (rs, rowNumber) -> {
			final Country country = new Country(rs.getInt("id"), rs.getString("name"));
			return country;
		});
	}

	@Override
	public boolean exists(Country country) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, country.getName());
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Override
	public Country create(Country country) {
		checkCountry(country);
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("country")
				.usingGeneratedKeyColumns("id");
		
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", country.getName());
		
		final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		country.setId(id.intValue());
		
		return country;
	}
	
	private void checkCountry(Country country) {
		final boolean present = exists(country);
		
		if (present) {
			throw new AppliancesRequestException("Country with the specified name already exists!");
		}
	}
	
	private void checkCountry(int id, String name) {
		final Country country = new Country(id, name);
		checkCountry(country);
	}

	@Override
	public Country getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				final Country country = new Country(rs.getInt("id"), rs.getString("name"));
				return country;
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}

	@Override
	public boolean updateCountry(int id, String name) {
		checkCountry(id, name);
		return jdbcTemplate.update(update, name, id) == 1;
	}
	
	private void checkCountryReference(int id) {
		try {
			final boolean referenceExists = jdbcTemplate.queryForObject(countryReferenceExists, Boolean.class, id);
			if (referenceExists) {
				throw new AppliancesRequestException("There is a brand that refers to the deleted object!");
			}
		} catch (DataAccessException e) {
			// Nothing to do
		}
	}

	@Override
	public boolean delete(int id) {
		checkCountryReference(id);
		return jdbcTemplate.update(delete, id) == 1;
	}
	
}