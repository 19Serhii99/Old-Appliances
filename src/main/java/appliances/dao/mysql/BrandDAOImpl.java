package appliances.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import appliances.dao.BrandDAO;
import appliances.dao.CountryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Brand;
import appliances.models.Country;

@Transactional
public class BrandDAOImpl implements BrandDAO {
	
	private final JdbcTemplate jdbcTemplate;
	
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final static String getAll = "SELECT b.id, b.name, c.id, c.name FROM brand b, country c WHERE b.id_country = c.id";
	
	private final static String getById = getAll + " AND b.id = ? LIMIT 1";
	
	private final static String getByName = getAll + " AND b.name = ? LIMIT 1";
	
	private final static String getRange = getAll + " AND b.id IN (:brands)";
	
	private final static String exists = "SELECT 1 FROM brand WHERE EXISTS (SELECT * FROM brand WHERE name = ?) LIMIT 1";
	
	private final static String getProductByBrandId = "SELECT 1 FROM product WHERE id_brand = ? LIMIT 1";
	
	private final static String update = "UPDATE brand SET name = ?, id_country = ? WHERE id = ?";
	
	private final static String delete = "DELETE FROM brand WHERE id = ?";
	
	private final static String getBrandList = "SELECT DISTINCT id_brand FROM product WHERE id_category = ?";
	
	private final CountryDAO countryDAO;
	
	public BrandDAOImpl(JdbcTemplate jdbcTemplate, CountryDAO countryDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.countryDAO = countryDAO;
	}
	
	@Override
	public List<Brand> getAll() {
		return jdbcTemplate.query(getAll, (rs, rowNum) -> {
			return fetchBrand(rs);
		});
	}
	
	private Brand fetchBrand(ResultSet rs) throws SQLException {
		final Brand brand = new Brand(rs.getInt("b.id"));
		brand.setName(rs.getString("b.name"));
		
		final Country country = new Country(rs.getInt("c.id"), rs.getString("c.name"));
		brand.setCountry(country);
		
		return brand;
	}
	
	@Override
	public boolean exists(Brand brand) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, brand.getName());
		} catch (DataAccessException e) {
			return false;
		}
	}
	
	@Transactional
	@Override
	public Brand create(Brand brand) {
		final Country country = checkBeforeCreation(brand);
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("brand")
				.usingGeneratedKeyColumns("id");
		
		final Map<String, Object> parameters = new HashMap<>();
		
		parameters.put("name", brand.getName());
		parameters.put("id_country", country.getId());
		
		final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		
		brand.setId(id.intValue());
		brand.setCountry(country);
		
		return brand;
	}
	
	private Country checkBeforeCreation(Brand brand) {
		if (exists(brand)) {
			throw new AppliancesRequestException("Brand with the specified name already exists!");
		}
		
		if (brand.getCountry() == null) {
			throw new AppliancesRequestException("Country must be present!");
		}
		
		if (brand.getCountry().getId() == 0) {
			throw new AppliancesRequestException("Country id must be present!");
		}
		
		final Country country = getCountryOfBrand(brand);
		
		if (country == null) {
			throw new AppliancesRequestException("Invalid country id!");
		}
		
		return country;
	}
	
	private Country getCountryOfBrand(Brand brand) {
		return countryDAO.getById(brand.getCountry().getId());
	}

	@Transactional
	@Override
	public boolean update(Brand brand) {
		if (brand.getId() == 0) {
			throw new AppliancesRequestException("Brand id must be present!");
		}
		
		final Brand brandDb = getById(brand.getId());
		if (brandDb == null) {
			throw new AppliancesRequestException("Invalid brand id!");
		}
		
		if (exists(brand)) {
			try {
				final Brand another = jdbcTemplate.queryForObject(getByName, (rs, rowNum) -> {
					return fetchBrand(rs);
				});
				
				if (another != null && another.getId() != brand.getId()) {
					throw new AppliancesRequestException("Brand with the specified name already exists!");
				}
			} catch (DataAccessException e) {
				// Nothing to do
			}
		}
		
		if (brand.getCountry().getId() == 0) {
			throw new AppliancesRequestException("Country id must be present!");
		}
		
		if (brand.getCountry().getId() != brandDb.getCountry().getId()) {
			final Country country = countryDAO.getById(brand.getCountry().getId());
			
			if (country == null) {
				throw new AppliancesRequestException("Invalid country id!");
			}
			
			brand.setCountry(country);
		}
		
		return jdbcTemplate.update(update, brand.getName(), brand.getCountry().getId(), brand.getId()) == 1;
	}

	@Transactional
	@Override
	public boolean delete(int id) {
		boolean productWithSuchBrandExists = false;
		
		try {
			productWithSuchBrandExists = jdbcTemplate.queryForObject(getProductByBrandId, Boolean.class);
		} catch (DataAccessException e) {
			// Nothing to do
		}
		
		if (productWithSuchBrandExists) {
			throw new AppliancesRequestException("There is a product that refers to the deleted object!");
		}
		
		return jdbcTemplate.update(delete, id) == 1;
	}

	@Override
	public Brand getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				return fetchBrand(rs);
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Brand> getAllByCategory(int categoryId) {
		final List<Integer> brandList = getBrandList(categoryId);
		final NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		
		final MapSqlParameterSource source = new MapSqlParameterSource();
		source.addValue("brands", brandList);
		
		if (brandList.isEmpty()) return Collections.emptyList();
		
		return template.query(getRange, source, (rs, rowNum) -> {
			return fetchBrand(rs);
		});
	}
	
	private List<Integer> getBrandList(int categoryId) {
		return jdbcTemplate.query(getBrandList, (rs, rowNum) -> {
			return rs.getInt(1);
		}, categoryId);
	}
	
}