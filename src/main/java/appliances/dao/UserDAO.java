package appliances.dao;

import appliances.models.User;

public interface UserDAO {
	
	boolean create(User user);
	boolean exists(User user);
	boolean update(User user);
	boolean updatePassword(int id, String password);
	boolean delete(int id);
	
	User signIn(String email, String password);
	User getById(int id);
	
}