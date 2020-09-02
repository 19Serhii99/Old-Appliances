package appliances.services;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.UserDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.User;

@Service
public class UserService {

	private final UserDAO userDAO;

	public UserService() {
		this.userDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getUserDAO();
	}

	public void create(User user) {
		userDAO.create(user);
	}

	public User signIn(String email, String password) {
		final User user = userDAO.signIn(email, password);
		
		if (user == null) {
			throw new AppliancesRequestException("Invalid email or password!");
		}
		
		return user;
	}
	
	public User getById(int id) {
		return userDAO.getById(id);
	}
	
	public void update(User user) {
		final boolean result = userDAO.update(user);
		
		if (!result) {
			throw new AppliancesRequestException("No user has been updated!");
		}
	}
	
	public void updatePassword(int id, String password) {
		userDAO.updatePassword(id, password);
	}
	
	public void delete(int id) {
		final boolean result = userDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No user has been deleted!");
		}
	}

}