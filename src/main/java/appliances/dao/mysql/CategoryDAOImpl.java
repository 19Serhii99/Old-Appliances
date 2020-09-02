package appliances.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import appliances.dao.CategoryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Category;

@Transactional
public class CategoryDAOImpl implements CategoryDAO {
	
	private final JdbcTemplate jdbcTemplate;
	
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final static String getAll = "SELECT c1.id, c1.name, c2.id, c2.name FROM category c1 LEFT JOIN category c2 ON c1.parent_id = c2.id";
	
	private final static String exists = "SELECT 1 FROM category WHERE EXISTS (SELECT * FROM category WHERE name = ?)";
	
	private final static String getById = getAll + " WHERE c1.id = ? LIMIT 1";
	
	private final static String update = "UPDATE category SET name = ?, parent_id = ? WHERE id = ?";
	
	private final static String delete = "DELETE FROM category WHERE id = ?";
	
	private final static String referenceExists = "SELECT 1 FROM product WHERE EXISTS (SELECT * FROM product WHERE id_category = ?)";
	
	public CategoryDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Category> getAll() {
		return jdbcTemplate.query(getAll, (rs, rowNumber) -> {
			return getCategoryFromResultSet(rs);
		});
	}
	
	@Override
	public Category getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				return getCategoryFromResultSet(rs);
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}

	private Category getCategoryFromResultSet(ResultSet rs) throws SQLException {
		final Category category = new Category(rs.getInt("c1.id"), rs.getString("c1.name"));
		
		final int parentId = rs.getInt("c2.id");
		if (!rs.wasNull()) {
			final Category parentCategory = new Category(parentId, rs.getString("c2.name"));
			category.setParent(parentCategory);
		}
		
		return category;
	}
	
	@Transactional
	@Override
	public Category create(Category category) {
		checkCategory(category);
		checkParentDataInCategory(category);
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("category")
				.usingGeneratedKeyColumns("id");
		
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", category.getName());
		
		if (category.getParent() != null) {
			parameters.put("parent_id", category.getParent().getId());
		}
		
		final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		category.setId(id.intValue());
		
		return category;
	}
	
	private void checkCategory(Category category) {
		final boolean present = exists(category);
		
		if (present) {
			throw new AppliancesRequestException("Category with the specified name already exists!");
		}
	}
	
	private void checkParentDataInCategory(Category category) {
		if (category.getParent() == null) return;
		
		if (category.getParent().getId() == 0) {
			throw new AppliancesRequestException("Parent id must be present!");
		}
		
		final Category parent = getById(category.getParent().getId());
		
		checkParentData(parent);
	}
	
	private void checkParentData(Category parent) {
		if (parent == null) {
			throw new AppliancesRequestException("Invalid parent id!");
		}
		
		if (parent.getParent() != null) {
			throw new AppliancesRequestException("You cannot create a new category as the third level!");
		}
	}

	@Override
	public boolean exists(Category category) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, category.getName());
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Transactional
	@Override
	public boolean update(Category category) {
		if (category.getId() == 0) {
			throw new AppliancesRequestException("Invalid category id!");
		}
		
		checkCategory(category);
		
		final Category categoryDb = getById(category.getId());
		
		if (categoryDb == null) {
			throw new AppliancesRequestException("Invalid category id!");
		}
		
		Category parentCategory = null;
		
		if (category.getParent() != null && category.getParent().getId() != categoryDb.getParent().getId()) {
			final Category parent = getById(category.getParent().getId());
			
			if (parent == null) {
				throw new AppliancesRequestException("Invalid parent id!");
			}
			
			if (parent.getParent() != null) {
				throw new AppliancesRequestException("You cannot create a new category as the third level!");
			}
			
			if (parent.getId() == categoryDb.getId()) {
				throw new AppliancesRequestException("Category and its parent cannot be the same!");
			}
			
			parentCategory = parent;
		}
		
		return jdbcTemplate.update(update, category.getName(), parentCategory.getId(), category.getId()) == 1;
	}
		

	@Override
	public boolean delete(int id) {
		checkCategoryReference(id);
		return jdbcTemplate.update(delete, id) == 1;
	}
	
	private void checkCategoryReference(int id) {
		try {
			final boolean refExists = jdbcTemplate.queryForObject(referenceExists, Boolean.class);
			if (refExists) {
				throw new AppliancesRequestException("There is a product that refers to the deleted object!");
			}
		} catch (DataAccessException e) {
			// Nothing to do
		}
	}
}