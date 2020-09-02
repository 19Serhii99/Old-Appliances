package appliances.dao;

import java.util.List;

import appliances.models.Status;

public interface StatusDAO {
	
	List<Status> getAll();
	
	Status getById(int id);
	Status getByName(String name);
	Status create(Status status);
	
	boolean exists(Status status);
	boolean update(int id, String name);
	boolean delete(int id);
	
}