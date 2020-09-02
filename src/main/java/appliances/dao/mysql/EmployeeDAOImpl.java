package appliances.dao.mysql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import appliances.dao.EmployeeDAO;
import appliances.dao.PositionDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Employee;
import appliances.models.Position;

@Transactional
public class EmployeeDAOImpl implements EmployeeDAO {
	
	private final JdbcTemplate jdbcTemplate;
	
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final PositionDAO positionDAO;
	
	private final static String exists = "SELECT 1 FROM position WHERE EXISTS (SELECT * FROM position WHERE email = ?) LIMIT 1";
	
	private final static String getAll = "SELECT e.id, e.last_name, e.first_name, e.middle_name, e.phone, e.email, e.start_date, p.id, p.name "
			+ "FROM employee e, position p WHERE e.id_position = p.id";
	
	private final static String signIn = getAll + " AND e.email = ? AND e.password = ?";
	
	private final static String getById = getAll + " AND e.id = ?";
	
	private final static String getByEmail = getAll + " AND e.email = ?";
	
	private final static String update = "UPDATE employee SET phone = ?, email = ?, last_name = ?, first_name = ?,"
			+ " middle_name = ? WHERE id = ?";
	
	private final static String updatePassword = "UPDATE employee SET password = ? WHERE id = ?";
	
	private final static String delete = "DELETE FROM employee WHERE id = ?";
	
	public EmployeeDAOImpl(JdbcTemplate jdbcTemplate, PositionDAO positionDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.positionDAO = positionDAO;
	}
	
	@Transactional
	@Override
	public Employee create(Employee employee) {
		checkEmployee(employee);
		
		final Position position = positionDAO.getById(employee.getPosition().getId());
		checkPosition(position);
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("employee")
				.usingGeneratedKeyColumns("id");
		
		final Date startDate = new Date(employee.getStartDate().getTime());
		
		final Map<String, Object> parameters = new HashMap<>();
		
		parameters.put("last_name", employee.getLastName());
		parameters.put("first_name", employee.getFirstName());
		parameters.put("middle_name", employee.getMiddleName());
		parameters.put("phone", employee.getPhone());
		parameters.put("email", employee.getEmail());
		parameters.put("password", employee.getPassword());
		parameters.put("start_date", startDate);
		parameters.put("id_position", position.getId());
		
		final Number number = simpleJdbcInsert.executeAndReturnKey(parameters);
		
		employee.setId(number.intValue());
		employee.setPosition(position);
		
		return employee;
	}
	
	private void checkPosition(Position position) {
		if (position == null) {
			throw new AppliancesRequestException("Invalid position id!");
		}
	}
	
	private void checkEmployee(Employee employee) {
		final boolean present = exists(employee);
		
		if (present) {
			final FieldExceptionObject error = new FieldExceptionObject("Employee with the specified email already exists!", FieldErrorType.EMAIL);
			throw new FormRequestException(error);
		}
		
		if (employee.getPosition().getId() == 0) {
			throw new AppliancesRequestException("Position id must be present!");
		}
	}
	
	@Override
	public boolean exists(Employee employee) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, employee.getEmail());
		} catch (DataAccessException e) {
			return false;
		}
	}
	
	@Override
	public Employee signIn(String email, String password) {
		try {
			return jdbcTemplate.queryForObject(signIn, (rs, rowNum) -> {
				return formEmployee(rs);
			}, email, password);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	@Override
	public boolean update(Employee employee) {
		if (exists(employee)) {
			try {
				final Employee another = jdbcTemplate.queryForObject(getByEmail, (rs, rowNumber) -> {
					return formEmployee(rs);
				}, employee.getEmail());
				
				if (another != null && employee.getId() != another.getId()) {
					throw new AppliancesRequestException("Employee with the specified email already exists!");
				}
			} catch (DataAccessException e) {
				// Nothing to do
			}
		}
		
		final PreparedStatementSetter pss = ps -> {
			ps.setString(1, employee.getPhone());
			ps.setString(2, employee.getEmail());
			ps.setString(3, employee.getLastName());
			ps.setString(4, employee.getFirstName());
			ps.setString(5, employee.getMiddleName());
			ps.setInt(6, employee.getId());
		};
		
		return jdbcTemplate.update(update, pss) == 1;
	}

	@Override
	public Employee getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				return formEmployee(rs);
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	private Employee formEmployee(ResultSet rs) throws SQLException {
		final Employee employee = new Employee(rs.getInt("e.id"));
		
		employee.setPhone(rs.getString("e.phone"));
		employee.setEmail(rs.getString("e.email"));
		employee.setLastName(rs.getString("e.last_name"));
		employee.setFirstName(rs.getString("e.first_name"));
		employee.setMiddleName(rs.getString("e.middle_name"));
		employee.setStartDate(rs.getDate("e.start_date"));
		
		final Position position = new Position(rs.getInt("p.id"), rs.getString("p.name"));
		employee.setPosition(position);
		
		return employee;
	}
	
	@Override
	public boolean delete(int id) {
		return jdbcTemplate.update(delete, id) == 1;
	}

	@Override
	public boolean updatePassword(int id, String password) {
		return jdbcTemplate.update(updatePassword, password, id) == 1;
	}
	
}