package appliances.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import appliances.dao.StatusDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Status;

public class StatusDAOImpl implements StatusDAO {
	
	private final JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final static String getAll = "SELECT id, name FROM status";
	
	private final static String getById = getAll + " WHERE id = ? LIMIT 1";
	
	private final static String getByName = getAll + " WHERE name = ? LIMIT 1";
	
	private final static String exists = "SELECT 1 FROM status WHERE EXISTS (SELECT * FROM status WHERE name = ?) LIMIT 1";
	
	private final static String update = "UPDATE status SET name = ? WHERE id = ?";
	
	private final static String delete = "DELETE from status WHERE id = ?";
	
	public StatusDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Status> getAll() {
		return jdbcTemplate.query(getAll + " ORDER BY id", (rs, rowNumber) -> {
			return fetchStatus(rs);
		});
	}

	@Override
	public Status create(Status status) {
		checkStatus(status);
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("status")
				.usingGeneratedKeyColumns("id");
		
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", status.getName());
		
		final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		status.setId(id.intValue());
		
		return status;
	}
	
	private void checkStatus(Status status) {
		final boolean present = exists(status);
		
		if (present) {
			throw new AppliancesRequestException("Status with the specified name already exists!");
		}
	}
	
	private void checkStatus(int id, String name) {
		final Status status = new Status(id, name);
		checkStatus(status);
	}
	
	@Override
	public boolean exists(Status status) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, status.getName());
		} catch (DataAccessException e) {
			return false;
		}
	}
	
	@Override
	public boolean update(int id, String name) {
		checkStatus(id, name);
		return jdbcTemplate.update(update, name, id) == 1;
	}
	
	@Override
	public boolean delete(int id) {
		return jdbcTemplate.update(delete, id) == 1;
	}
	
	@Override
	public Status getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNumber) -> {
				return fetchStatus(rs);
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}

	@Override
	public Status getByName(String name) {
		try {
			return jdbcTemplate.queryForObject(getByName, (rs, rowNum) -> {
				return fetchStatus(rs);
			}, name);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	private Status fetchStatus(ResultSet rs) throws SQLException {
		final Status status = new Status(rs.getInt("id"), rs.getString("name"));
		return status;
	}
}