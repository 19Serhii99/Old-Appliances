package appliances.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import appliances.dao.BrandDAO;
import appliances.dao.CategoryDAO;
import appliances.dao.CountryDAO;
import appliances.dao.EmployeeDAO;
import appliances.dao.OrderDAO;
import appliances.dao.PositionDAO;
import appliances.dao.ProductDAO;
import appliances.dao.StatusDAO;
import appliances.dao.UserDAO;
import appliances.dao.mongodb.Autoincrement;
import appliances.dao.mongodb.BrandDAOImpl;
import appliances.dao.mongodb.CategoryDAOImpl;
import appliances.dao.mongodb.CountryDAOImpl;
import appliances.dao.mongodb.EmployeeDAOImpl;
import appliances.dao.mongodb.OrderDAOImpl;
import appliances.dao.mongodb.PositionDAOImpl;
import appliances.dao.mongodb.ProductDAOImpl;
import appliances.dao.mongodb.StatusDAOImpl;
import appliances.dao.mongodb.UserDAOImpl;

public class MongoDBFactory implements IFactory {
	
	private static MongoDBFactory instance;
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	@SuppressWarnings("resource")
	private MongoDBFactory() {
		final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MongoDBConfig.class);
		
		mongoTemplate = context.getBean(MongoTemplate.class);
		autoincrement = new Autoincrement(mongoTemplate);
	}
	
	public static MongoDBFactory getInstance() {
		if (instance == null) {
			synchronized (MongoDBFactory.class) {
				if (instance == null) {
					instance = new MongoDBFactory();
				}
			}
		}
		return instance;
	}

	@Override
	public BrandDAO getBrandDAO() {
		return new BrandDAOImpl(mongoTemplate, autoincrement);
	}

	@Override
	public CategoryDAO getCategoryDAO() {
		return new CategoryDAOImpl(mongoTemplate, autoincrement);
	}

	@Override
	public CountryDAO getCountryDAO() {
		return new CountryDAOImpl(mongoTemplate, autoincrement);
	}

	@Override
	public EmployeeDAO getEmployeeDAO() {
		return new EmployeeDAOImpl(mongoTemplate, autoincrement);
	}

	@Override
	public OrderDAO getOrderDAO() {
		return new OrderDAOImpl(mongoTemplate, autoincrement, getUserDAO(), getEmployeeDAO(), getStatusDAO(), getProductDAO());
	}

	@Override
	public PositionDAO getPositionDAO() {
		return new PositionDAOImpl(mongoTemplate, autoincrement);
	}

	@Override
	public ProductDAO getProductDAO() {
		return new ProductDAOImpl(mongoTemplate, autoincrement, getCategoryDAO(), getBrandDAO());
	}

	@Override
	public StatusDAO getStatusDAO() {
		return new StatusDAOImpl(mongoTemplate, autoincrement);
	}

	@Override
	public UserDAO getUserDAO() {
		return new UserDAOImpl(mongoTemplate, autoincrement);
	}
}