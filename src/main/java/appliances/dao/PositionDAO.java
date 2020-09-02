package appliances.dao;

import java.util.List;

import appliances.models.Position;

public interface PositionDAO {
	
	List<Position> getAll();
	Position getById(int id);
	
	Position create(Position position);
	boolean exists(Position position);
	boolean update(int id, String name);
	boolean delete(int id);
	
}