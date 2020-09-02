package appliances.config;

public class DatabaseFactory {
	
	private static volatile DatabaseFactory instance;
	
	private DatabaseFactory () {}
	
	public static DatabaseFactory getInstance() {
		if (instance == null) {
			synchronized (DatabaseFactory.class) {
				if (instance == null) {
					instance = new DatabaseFactory();
				}
			}
		}
		return instance;
	}
	
	public IFactory getFactory(DAOType type) {
		if (type == DAOType.MySQL) {
			return MySQLFactory.getInstance();
		}
		
		if (type == DAOType.MongoDB) {
			return MongoDBFactory.getInstance();
		}
		
		return null;
	}
}