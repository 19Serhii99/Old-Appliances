package appliances.services;

import java.util.List;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.StatusDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Status;

@Service
public class StatusService {

	private final StatusDAO statusDAO;

	public StatusService() {
		this.statusDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getStatusDAO();
	}
	
	public List<Status> getAll() {
		return statusDAO.getAll();
	}
	
	public Status getById(int id) {
		return statusDAO.getById(id);
	}
	
	public Status getByName(String name) {
		return statusDAO.getByName(name);
	}
	
	public Status create(Status status) {
		return statusDAO.create(status);
	}
	
	public void update(int id, String name) {
		final boolean result = statusDAO.update(id, name);
		
		if (!result) {
			throw new AppliancesRequestException("No status has been updated!");
		}
	}
	
	public void delete(int id) {
		final boolean result = statusDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No status has been deleted!");
		}
	}
	
}