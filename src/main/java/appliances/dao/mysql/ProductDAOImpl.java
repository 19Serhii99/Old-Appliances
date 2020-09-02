package appliances.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import appliances.dao.BrandDAO;
import appliances.dao.CategoryDAO;
import appliances.dao.ProductDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Brand;
import appliances.models.Category;
import appliances.models.Country;
import appliances.models.Product;

public class ProductDAOImpl implements ProductDAO {
	
	private final JdbcTemplate jdbcTemplate;
	
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final static String getAll = "SELECT p.id, p.name, p.price, p.amount, p.width, p.height, p.depth, p.weight, p.hidden, b.id, "
			+ "b.name, c.id, c.name, cat.id, cat.name FROM product p, brand b, country c, category cat WHERE p.id_category = cat.id AND "
			+ "p.id_brand = b.id AND b.id_country = c.id";
	
	private final static String getById = getAll + " AND p.id = ? LIMIT 1";
	
	private final static String getByCategory = getAll + " AND p.id_category = ? AND p.hidden = false";
	
	private final static String search = getAll + " AND (p.name LIKE ? OR b.name LIKE ?)";
	
	private final static String exists = "SELECT 1 FROM product WHERE EXISTS (SELECT * FROM product WHERE name = ?) LIMIT 1";
	
	private final static String show = "UPDATE product SET hidden = false WHERE id = ?";
	
	private final static String hide = "UPDATE product SET hidden = true WHERE id = ?";
	
	private final static String update = "UPDATE product SET name = ?, price = ?, amount = ?, width = ?, height = ?, depth = ?, weight = ?,"
			+ "hidden = ?, id_category = ?, id_brand = ? WHERE id = ?";
	
	private final static String delete = "DELETE FROM product WHERE id = ?";
	
	private final CategoryDAO categoryDAO;
	private final BrandDAO brandDAO;
	
	public ProductDAOImpl(JdbcTemplate jdbcTemplate, CategoryDAO categoryDAO, BrandDAO brandDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.categoryDAO = categoryDAO;
		this.brandDAO = brandDAO;
	}

	@Override
	public List<Product> getAll(int categoryId) {
		return jdbcTemplate.query(getByCategory, (rs, rowNum) -> {
			return formProduct(rs); 
		}, categoryId);
	}
	
	private Product formProduct(ResultSet rs) throws SQLException {
		final Country country = new Country(rs.getInt("c.id"));
		country.setName(rs.getString("c.name"));
		
		final Category category = new Category(rs.getInt("cat.id"));
		category.setName(rs.getString("cat.name"));
		
		final Brand brand = new Brand(rs.getInt("b.id"));
		brand.setName(rs.getString("b.name"));
		brand.setCountry(country);
		
		final Product product = new Product(rs.getInt("p.id"));
		product.setName(rs.getString("p.name"));
		product.setPrice(rs.getFloat("p.price"));
		product.setAmount(rs.getInt("p.amount"));
		product.setHidden(rs.getBoolean("p.hidden"));
		
		final float width = rs.getFloat("p.width");
		if (!rs.wasNull()) product.setWidth(width);
		
		final float height = rs.getFloat("p.height");
		if (!rs.wasNull()) product.setHeight(height);
		
		final float depth = rs.getFloat("p.depth");
		if (!rs.wasNull()) product.setDepth(depth);
		
		final float weight = rs.getFloat("p.weight");
		if (!rs.wasNull()) product.setWeight(weight);
		
		product.setBrand(brand);
		product.setCategory(category);
		
		return product;
	}
	
	@Override
	public List<Product> getAllByFilter(int categoryId, Map<String, List<String>> filter) {
		final List<String> opts = new LinkedList<>();
		final List<String> sort = new LinkedList<>();
		
		filter.entrySet().forEach(item-> checkFilterConditions(opts, sort, item));
		
		final StringBuilder query = new StringBuilder(getAll);
		opts.forEach(item -> query.append(item));
		
		if (!sort.isEmpty()) {
			sort.forEach(item -> query.append(item));
		}
		
		return jdbcTemplate.query(query.toString(), (rs, rowNum) -> {
			return formProduct(rs);
		});
	}
	
	private void checkFilterConditions(List<String> opts, List<String> sort, Entry<String, List<String>> conditions) {
		final String condition = conditions.getKey();
		final List<String> value = conditions.getValue();
		
		if (condition.equals("brands")) {
			makeBrandsFilter(opts, value);
			return;
		}
		
		if (condition.equals("price")) {
			makePriceFilter(opts, value);
			return;
		}
		
		if (condition.equals("visibility")) {
			makeVisibilityFilter(opts, value.get(0));
			return;
		}
		
		if (condition.equals("sort")) {
			makeSortFilter(sort, value.get(0));
			return;
		}
	}

	
	private void makeBrandsFilter(List<String> opts, List<String> values) {
		final StringBuilder query = new StringBuilder(" AND p.id_brand IN (");
		
		values.forEach(brand -> {
			query.append(brand + ",");
		});
		
		query.deleteCharAt(query.length() - 1);
		query.append(")");
		
		opts.add(query.toString());
	}
	
	private void makePriceFilter(List<String> opts, List<String> values) {
		final String query = " AND p.price BETWEEN " + values.get(0)
			+ " AND " + values.get(1);
		opts.add(query);
	}
	
	private void makeVisibilityFilter(List<String> opts, String value) {
		final String query = " AND p.hidden = " + value.equals("1");
		opts.add(query);
	}
	
	private void makeSortFilter(List<String> opts, String sort) {
		if (sort.equals("expensive-cheap")) {
			opts.add(" ORDER BY p.price DESC");
			return;
		}
		
		if (sort.equals("cheap-expensive")) {
			opts.add(" ORDER BY p.price");
			return;
		}
		
		if (sort.equals("end-start")) {
			opts.add(" ORDER BY p.id");
			return;
		}
		
		if (sort.equals("start-end")) {
			opts.add(" ORDER BY p.id DESC");
			return;
		}
	}
	
	@Override
	public List<Product> search(String text) {
		text = "%" + text + "%";
		
		return jdbcTemplate.query(search, (rs, rowNum) -> {
			return formProduct(rs);
		}, text, text);
	}

	@Override
	public Product getById(int productId) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				return formProduct(rs);
			}, productId);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	private void checkProduct(Product product) {
		final boolean present = exists(product);
		
		if (present) {
			final FieldExceptionObject error = new FieldExceptionObject("Product with the specified name already exists!", FieldErrorType.NAME);
			throw new FormRequestException(error);
		}
	}
	
	private void checkCategory(Category category) {
		if (category == null) {
			throw new AppliancesRequestException("Invalid category id!");
		}
	}
	
	private void checkBrand(Brand brand) {
		if (brand == null) {
			throw new AppliancesRequestException("Invalid brand id!");
		}
	}
	
	@Override
	public Product create(Product product) {
		checkProduct(product);
		
		final Category category = categoryDAO.getById(product.getCategory().getId());
		checkCategory(category);
		
		final Brand brand = brandDAO.getById(product.getBrand().getId());
		checkBrand(brand);
		
		final int categoryId = product.getCategory().getId();
		final int brandId = product.getBrand().getId();
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("product")
				.usingGeneratedKeyColumns("id", "adding_time");
		
		final Map<String, Object> parameters = new HashMap<>();
		
		parameters.put("name", product.getName());
		parameters.put("price", product.getPrice());
		parameters.put("amount", product.getAmount());
		parameters.put("width", product.getWidth());
		parameters.put("height", product.getHeight());
		parameters.put("depth", product.getDepth());
		parameters.put("weight", product.getWeight());
		parameters.put("hidden", false);
		parameters.put("id_category", categoryId);
		parameters.put("id_brand", brandId);
		
		final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		
		product.setId(id.intValue());
		product.setCategory(category);
		product.setBrand(brand);
		
		return product;
	}

	@Override
	public boolean exists(Product product) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, product.getName());
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Override
	public boolean update(Product product) {
		final Product productDb = getById(product.getId());
		
		if (productDb == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
		
		if (productDb.getName().equals(product.getName()) && productDb.getId() != product.getId()) {
			throw new AppliancesRequestException("Product with the specified name already exists!");
		}
		
		final Category category = categoryDAO.getById(product.getCategory().getId());
		checkCategory(category);
		
		final Brand brand = brandDAO.getById(product.getBrand().getId());
		checkBrand(brand);
		
		if (product.getId() == 0) {
			throw new AppliancesRequestException("Product id must be present!");
		}
		
		if (getById(product.getId()) == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
		
		final int categoryId = product.getCategory().getId();
		final int brandId = product.getBrand().getId();
		
		final PreparedStatementSetter pss = ps -> {
			ps.setString(1, product.getName());
			ps.setFloat(2, product.getPrice());
			ps.setInt(3, product.getAmount());
			
			if (product.getWidth() != null) {
				ps.setFloat(4, product.getWidth());
			} else {
				ps.setNull(4, Types.FLOAT);
			}
			
			if (product.getHeight() != null) {
				ps.setFloat(5, product.getHeight());
			} else {
				ps.setNull(5, Types.FLOAT);
			}
			
			if (product.getDepth() != null) {
				ps.setFloat(6, product.getDepth());
			} else {
				ps.setNull(6, Types.FLOAT);
			}
			
			if (product.getWeight() != null) {
				ps.setFloat(7, product.getWeight());
			} else {
				ps.setNull(7, Types.FLOAT);
			}
			
			ps.setBoolean(8, product.isHidden());
			ps.setInt(9, categoryId);
			ps.setInt(10, brandId);
			ps.setInt(11, product.getId());
		};
		
		return jdbcTemplate.update(update, pss) == 1;
	}

	@Override
	public boolean show(int id) {
		return jdbcTemplate.update(show, id) == 1;
	}

	@Override
	public boolean hide(int id) {
		return jdbcTemplate.update(hide, id) == 1;
	}

	@Override
	public boolean delete(int id) {
		return jdbcTemplate.update(delete, id) == 1;
	}

	@Override
	public float getMinPrice(int categoryId, Map<String, List<String>> filter) {
		return getPriceValue(categoryId, filter, Sort.Direction.ASC);
	}

	@Override
	public float getMaxPrice(int categoryId, Map<String, List<String>> filter) {
		return getPriceValue(categoryId, filter, Sort.Direction.DESC);
	}
	
	private float getPriceValue(int categoryId, Map<String, List<String>> filter, Sort.Direction direction) {
		final List<Product> filteredProducts = filter == null ? getAll(categoryId) : getAllByFilter(categoryId, filter);
		if (filteredProducts.isEmpty()) return 0.0f;
		
		final Map<Float, List<Product>> groupedProducts = filteredProducts.stream().collect(Collectors.groupingBy(Product::getPrice));
		
		Optional<Entry<Float, List<Product>>> optional = null;
		
		if (direction == Sort.Direction.ASC) {
			optional = groupedProducts.entrySet().stream().min(Comparator.comparing(Map.Entry::getKey));
		} else {
			optional = groupedProducts.entrySet().stream().max(Comparator.comparing(Map.Entry::getKey));
		}
	
		return optional.get().getKey();
	}

	/** 
	 * This function is only used with MongoDB DAO 
	 * **/
	@Override
	public void fillRandomData() {}
}