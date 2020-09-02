package appliances.dao.mysql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.transaction.annotation.Transactional;

import appliances.dao.UserDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.User;

@Transactional
public class UserDAOImpl implements UserDAO {
	
	private final JdbcTemplate jdbcTemplate;
	
	private final static String insert = "INSERT INTO user (phone, email, password, last_name, first_name, middle_name) "
			+ "VALUES (?, ?, ?, ?, ?, ?)";
	
	private final static String exists = "SELECT 1 FROM user WHERE EXISTS (SELECT * FROM user WHERE email = ?) LIMIT 1";
	
	private final static String getUser = "SELECT id, phone, email, last_name, first_name, middle_name, city, birthday FROM user ";
	
	private final static String signIn = getUser + "WHERE email = ? AND password = ? LIMIT 1";
	
	private final static String getById = getUser + "WHERE id = ? LIMIT 1";
	
	private final static String getByEmail = getUser + "WHERE email = ? LIMIT 1";
	
	private final static String update = "UPDATE user SET phone = ?, email = ?, last_name = ?, first_name = ?,"
			+ " middle_name = ?, city = ?, birthday = ? WHERE id = ?";
	
	private final static String updatePassword = "UPDATE user SET password = ? WHERE id = ?";
	
	private final static String delete = "DELETE FROM user WHERE id = ?";
	
	public UserDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public boolean create(User user) {
		checkUser(user);
		
		return jdbcTemplate.update(insert, user.getPhone(), user.getEmail(), user.getPassword(),
						user.getLastName(), user.getFirstName(), user.getMiddleName()) > 0;
	}
	
	private void checkUser(User user) {
		final boolean present = exists(user);
		if (present) {
			final FieldExceptionObject error = new FieldExceptionObject("User with the specified email already exists!", FieldErrorType.EMAIL);
			throw new FormRequestException(error);
		}
	}

	@Override
	public boolean exists(User user) {
		try {
			return jdbcTemplate.queryForObject(exists, Boolean.class, user.getEmail());
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Override
	public User signIn(String email, String password) {
		try {
			return jdbcTemplate.queryForObject(signIn, (rs, rowNum) -> {
				return formUser(rs);
			}, email, password);
		} catch (DataAccessException e) {
			return null;
		}
	}

	@Override
	public User getById(int id) {
		try {
			return jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				return formUser(rs);
			}, id);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	private User formUser(ResultSet rs) throws SQLException {
		final User user = new User(rs.getInt("id"));
		
		user.setPhone(rs.getString("phone"));
		user.setEmail(rs.getString("email"));
		user.setLastName(rs.getString("last_name"));
		user.setFirstName(rs.getString("first_name"));
		user.setMiddleName(rs.getString("middle_name"));
		user.setCity(rs.getString("city"));
		user.setBirthday(rs.getDate("birthday"));
		
		return user;
	}

	@Override
	public boolean update(User user) {
		if (exists(user)) {
			try {
				final User another = jdbcTemplate.queryForObject(getByEmail, (rs, rowNum) -> {
					return formUser(rs);
				}, user.getEmail());
				
				if (another != null && user.getId() != another.getId()) {
					throw new AppliancesRequestException("User with the specified email already exists!");
				}
			} catch (DataAccessException e) {
				// Nothing to do
			}
		}
		
		final PreparedStatementSetter pss = ps -> {
			Date birthday = null;
			if (user.getBirthday() != null) {
				birthday = new Date(user.getBirthday().getTime());
			}
			
			ps.setString(1, user.getPhone());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getLastName());
			ps.setString(4, user.getFirstName());
			ps.setString(5, user.getMiddleName());
			ps.setString(6, user.getCity());
			ps.setDate(7, birthday);
			ps.setInt(8, user.getId());
		};
		
		return jdbcTemplate.update(update, pss) == 1;
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