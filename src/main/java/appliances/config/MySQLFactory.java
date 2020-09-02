package appliances.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import appliances.dao.BrandDAO;
import appliances.dao.CategoryDAO;
import appliances.dao.CountryDAO;
import appliances.dao.EmployeeDAO;
import appliances.dao.OrderDAO;
import appliances.dao.PositionDAO;
import appliances.dao.ProductDAO;
import appliances.dao.StatusDAO;
import appliances.dao.UserDAO;
import appliances.dao.mysql.BrandDAOImpl;
import appliances.dao.mysql.CategoryDAOImpl;
import appliances.dao.mysql.CountryDAOImpl;
import appliances.dao.mysql.EmployeeDAOImpl;
import appliances.dao.mysql.OrderDAOImpl;
import appliances.dao.mysql.PositionDAOImpl;
import appliances.dao.mysql.ProductDAOImpl;
import appliances.dao.mysql.StatusDAOImpl;
import appliances.dao.mysql.UserDAOImpl;

public class MySQLFactory implements IFactory {
	
	private static volatile MySQLFactory instance;
	
	private final JdbcTemplate jdbcTemplate;
	
	@SuppressWarnings("resource")
	private MySQLFactory() {
		final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JdbcConfig.class);
		jdbcTemplate = context.getBean(JdbcTemplate.class);
	}
	
	public static MySQLFactory getInstance() {
		if (instance == null) {
			synchronized (MySQLFactory.class) {
				if (instance == null) {
					instance = new MySQLFactory();
				}
			}
		}
		return instance;
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public BrandDAO getBrandDAO() {
		return new BrandDAOImpl(jdbcTemplate, getCountryDAO());
	}

	@Override
	public CategoryDAO getCategoryDAO() {
		return new CategoryDAOImpl(jdbcTemplate);
	}

	@Override
	public CountryDAO getCountryDAO() {
		return new CountryDAOImpl(jdbcTemplate);
	}

	@Override
	public EmployeeDAO getEmployeeDAO() {
		return new EmployeeDAOImpl(jdbcTemplate, getPositionDAO());
	}

	@Override
	public OrderDAO getOrderDAO() {
		return new OrderDAOImpl(jdbcTemplate, getUserDAO(), getEmployeeDAO(), getStatusDAO(), getProductDAO());
	}

	@Override
	public PositionDAO getPositionDAO() {
		return new PositionDAOImpl(jdbcTemplate);
	}

	@Override
	public ProductDAO getProductDAO() {
		return new ProductDAOImpl(jdbcTemplate, getCategoryDAO(), getBrandDAO());
	}

	@Override
	public StatusDAO getStatusDAO() {
		return new StatusDAOImpl(jdbcTemplate);
	}

	@Override
	public UserDAO getUserDAO() {
		return new UserDAOImpl(jdbcTemplate);
	}
	
}