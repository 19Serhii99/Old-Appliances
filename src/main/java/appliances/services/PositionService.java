package appliances.services;

import java.util.List;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.PositionDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Position;

@Service
public class PositionService {
	
	private final PositionDAO positionDAO;

	public PositionService() {
		this.positionDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getPositionDAO();
	}
	
	public List<Position> getAll() {
		return positionDAO.getAll();
	}
	
	public Position getById(int id) {
		return positionDAO.getById(id);
	}
	
	public Position create(Position position) {
		return positionDAO.create(position);
	}
	
	public void update(int id, String name) {
		final boolean result = positionDAO.update(id, name);
		
		if (!result) {
			throw new AppliancesRequestException("No position has been updated!");
		}
	}
	
	public void delete(int id) {
		final boolean result = positionDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No position has been deleted!");
		}
	}
	
}