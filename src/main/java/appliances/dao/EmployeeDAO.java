package appliances.dao;

import appliances.models.Employee;

public interface EmployeeDAO {
	
	boolean exists(Employee employee);
	boolean update(Employee employee);
	boolean updatePassword(int id, String password);
	boolean delete(int id);
	
	Employee create(Employee employee);
	Employee signIn(String email, String password);
	Employee getById(int id);
	
}