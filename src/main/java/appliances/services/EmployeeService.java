package appliances.services;

import org.springframework.stereotype.Service;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.dao.EmployeeDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Employee;

@Service
public class EmployeeService {
	private final EmployeeDAO employeeDAO;

	public EmployeeService() {
		this.employeeDAO = DatabaseFactory.getInstance().getFactory(DAOType.MongoDB).getEmployeeDAO();
	}

	public Employee create(Employee employee) {
		return employeeDAO.create(employee);
	}

	public Employee signIn(String email, String password) {
		final Employee employee = employeeDAO.signIn(email, password);
		
		if (employee == null) {
			throw new AppliancesRequestException("Invalid email or password!");
		}
		
		return employee;
	}
	
	public Employee getById(int id) {
		return employeeDAO.getById(id);
	}
	
	public void update(Employee employee) {
		final boolean result = employeeDAO.update(employee);
		
		if (!result) {
			throw new AppliancesRequestException("No employee has been updated!");
		}
	}
	
	public void updatePassword(int id, String password) {
		employeeDAO.updatePassword(id, password);
	}
	
	public void delete(int id) {
		final boolean result = employeeDAO.delete(id);
		
		if (!result) {
			throw new AppliancesRequestException("No employee has been deleted!");
		}
	}
	
}