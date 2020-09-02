package appliances.dao.mysql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import appliances.dao.PositionDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Position;

public class PositionDAOImpl implements PositionDAO {
	
	private final JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final static String getAll = "SELECT id, name FROM position";
	
	private final static String getById = getAll + " WHERE id = ? LIMIT 1";
	
	private final static String exists = "SELECT 1 FROM position WHERE EXISTS (SELECT * FROM position WHERE name = ?) LIMIT 1";
	
	private final static String update = "UPDATE position SET name = ? WHERE id = ?";
	
	private final static String delete = "DELETE from position WHERE id = ?";
	
	public PositionDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Position> getAll() {
		return jdbcTemplate.query(getAll, (rs, rowNumber) -> {
			final Position position = new Position(rs.getInt("id"), rs.getString("name"));
			return position;
		});
	}

	@Override
	public Position create(Position position) {
		checkPosition(position);

		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("position")
				.usingGeneratedKeyColumns("id");
		
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", position.getName());
		
		final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		position.setId(id.intValue());
		
		return position;
	}
	
	private void checkPosition(int id, String name) {
		final Position position = new Position(id, name);
		checkPosition(position);
	}
	
	private void checkPosition(Position position) {
		final boolean present = exists(position);
		
		if (present) {
			throw new AppliancesRequestException("Position with the specified name already exists!");
		}
	}

	@Override
	public boolean exists(Position position) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, position.getName());
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Override
	public boolean update(int id, String name) {
		checkPosition(id, name);
		return jdbcTemplate.update(update, name, id) == 1;
	}

	@Override
	public boolean delete(int id) {
		return jdbcTemplate.update(delete, id) == 1;
	}

	@Override
	public Position getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				final Position position = new Position(rs.getInt("id"), rs.getString("name"));
				return position;
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
}