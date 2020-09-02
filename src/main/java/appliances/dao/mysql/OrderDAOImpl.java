package appliances.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import appliances.dao.EmployeeDAO;
import appliances.dao.OrderDAO;
import appliances.dao.ProductDAO;
import appliances.dao.StatusDAO;
import appliances.dao.UserDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Basket;
import appliances.models.Employee;
import appliances.models.Order;
import appliances.models.Product;
import appliances.models.Status;
import appliances.models.User;

@Transactional
public class OrderDAOImpl implements OrderDAO {
	
	private final JdbcTemplate jdbcTemplate;
	
	private SimpleJdbcInsert simpleJdbcInsert;
	
	private final UserDAO userDAO;
	private final EmployeeDAO employeeDAO;
	private final StatusDAO statusDAO;
	private final ProductDAO productDAO;
	
	private final static String insertBasket = "INSERT INTO basket (id_order, id_product, price, amount) VALUES (?, ?, ?, ?)";
	
	private final static String getAll = "SELECT o.id, o.id_user, o.id_employee, o.last_name, o.first_name, o.middle_name, o.phone, o.city, o.post_office, "
			+ "o.delivery_cost, o.total, o.creation_time, s.id, s.name FROM `order` o, status s WHERE o.id_status = s.id";
	
	private final static String getById = getAll + " AND o.id = ?";

	private final static String getNewAll = getAll + " AND o.id_status = ?";
	
	private final static String getAllFromBasket = "SELECT id_product, price, amount FROM basket WHERE id_order = ?";
	
	private final static String getNewStatus = "SELECT id FROM status WHERE name = 'New'";
	
	private final static String deleteBaskets = "DELETE FROM basket WHERE id_order = ?";
	
	private final static String delete = "DELETE FROM `order` WHERE id = ?";
	
	private final static String changeStatus = "UPDATE `order` SET id_status = ? WHERE id = ?";
	
	private final static String changeStatusAndEmployee = "UPDATE `order` SET id_status = ?, id_employee = ? WHERE id = ?";
	
	public OrderDAOImpl(JdbcTemplate jdbcTemplate, UserDAO userDAO, EmployeeDAO employeeDAO, StatusDAO statusDAO, ProductDAO productDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.userDAO = userDAO;
		this.employeeDAO = employeeDAO;
		this.statusDAO = statusDAO;
		this.productDAO = productDAO;
	}
	
	@Override
	public int create(Order order) {
		Integer userId = null;
		if (order.getUser() != null) userId = order.getUser().getId();
		
		final int statusId = order.getStatus().getId();
		
		simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
				.withTableName("`order`")
				.usingGeneratedKeyColumns("id", "creation_time", "processing", "executed", "canceled", "id_employee");
		
		final Map<String, Object> parameters = new HashMap<>();
		
		parameters.put("id_user", userId);
		parameters.put("last_name", order.getLastName());
		parameters.put("first_name", order.getFirstName());
		parameters.put("middle_name", order.getMiddleName());
		parameters.put("phone", order.getPhone());
		parameters.put("city", order.getCity());
		parameters.put("post_office", order.getPostOffice());
		parameters.put("delivery_cost", order.getDeliveryCost());
		parameters.put("id_status", statusId);
		parameters.put("total", order.getTotal());
		
		final Number orderId = simpleJdbcInsert.executeAndReturnKey(parameters);
		
		final BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final Basket basket = order.getBaskets().get(i);
				
				ps.setInt(1, orderId.intValue());
				ps.setInt(2, basket.getProduct().getId());
				
				ps.setFloat(3, basket.getPrice());
				ps.setInt(4, basket.getAmount());
			}
			
			@Override
			public int getBatchSize() {
				return order.getBaskets().size();
			}
		};
		
		jdbcTemplate.batchUpdate(insertBasket, bpss);
		
		return orderId.intValue();
	}

	@Override
	public List<Order> getAll() {
		final Integer statusId = getNewStatusId();
		
		if (statusId == null) {
			throw new AppliancesRequestException("Status is undefined!");
		}
		
		final List<Order> orders = jdbcTemplate.query(getNewAll, (rs, rowNum) -> {
			return fetchOrder(rs);
		}, statusId);
		
		orders.forEach(order -> {
			final List<Basket> baskets = jdbcTemplate.query(getAllFromBasket, (rs, rowNum) -> {
				return fetchBasket(rs);
			}, order.getId());
			order.setBaskets(baskets);
		});
		
		return orders;
	}
	
	private Order fetchOrder(ResultSet rs) throws SQLException {
		final Order order = new Order(rs.getInt("o.id"));
		
		order.setLastName(rs.getString("o.last_name"));
		order.setFirstName(rs.getString("o.first_name"));
		order.setMiddleName(rs.getString("o.middle_name"));
		
		order.setPhone(rs.getString("o.phone"));
		order.setCity(rs.getString("o.city"));
		order.setPostOffice(rs.getInt("o.post_office"));
		
		order.setDeliveryCost(rs.getFloat("o.delivery_cost"));
		order.setTotal(rs.getFloat("o.total"));
		order.setCreationTime(new Date(rs.getTimestamp("o.creation_time").getTime()));
		
		final Status status = new Status(rs.getInt("s.id"), rs.getString("s.name"));
		order.setStatus(status);
		
		final int userId = rs.getInt("o.id_user");
		if (!rs.wasNull()) {
			final User user = userDAO.getById(userId);
			order.setUser(user);
		}
		
		final int employeeId = rs.getInt("o.id_employee");
		if (!rs.wasNull()) {
			final Employee employee = employeeDAO.getById(employeeId);
			order.setEmployee(employee);
		}
		
		return order;
	}
	
	private Basket fetchBasket(ResultSet rs) throws SQLException {
		final Basket basket = new Basket();
		
		basket.setAmount(rs.getInt("amount"));
		basket.setPrice(rs.getFloat("price"));
		
		final int productId = rs.getInt("id_product");
		if (!rs.wasNull()) {
			final Product product = productDAO.getById(productId);
			basket.setProduct(product);
		}
		
		return basket;
	}
	
	private Integer getNewStatusId() {
		try {
			return jdbcTemplate.queryForObject(getNewStatus, Integer.class);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<Order> getAllByFilter(Map<String, List<String>> filter, int id) {
		final List<String> operations = new LinkedList<>();
		
		filter.forEach((key, values) -> {
			final String query = handleFilter(key, values, id);
			if (query != null) operations.add(query);
		});
		
		final boolean hasStatus = filter.entrySet()
			.stream()
			.filter(map -> "status".equals(map.getKey()))
			.findFirst()
			.isPresent();
		
		if (!hasStatus) {
			final Integer statusId = getNewStatusId();
			operations.add(" AND o.id_status = " + statusId);
		}
		
		final StringBuilder query = new StringBuilder(getAll);
		operations.forEach(item -> query.append(item));
		
		final List<Order> orders = jdbcTemplate.query(query.toString(), (rs, rowNum) -> {
			return fetchOrder(rs);
		});
		
		orders.forEach(order -> {
			final List<Basket> baskets = jdbcTemplate.query(getAllFromBasket, (rs, rowNum) -> {
				return fetchBasket(rs);
			}, order.getId());
			order.setBaskets(baskets);
		});
		
		return orders;
	}
	
	private String handleFilter(String key, List<String> values, int id) {
		if (key.equals("status")) {
			return handleStatus(values, id);
		}
		
		if (key.equals("date")) {
			return handleDate(values);
		}
		
		return null;
	}
	
	private String handleStatus(List<String> values, int id) {
		final int statusId = Integer.parseInt(values.get(0));
		final Status status = statusDAO.getById(statusId);
		
		if (status == null) return null;
		
		if ("New".equals(status.getName())) {
			return " AND o.id_status = " + statusId;
		}
		
		return " AND o.id_status = " + statusId + " AND o.id_employee = " + id;
	}
	
	private String handleDate(List<String> values) {
		try {
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Calendar calendar = Calendar.getInstance();
			
			calendar.setTime(formatter.parse(values.get(1)));
			calendar.add(Calendar.DATE, 1);
			
			final Date dateFrom = formatter.parse(values.get(0));
			final Date dateTo = calendar.getTime();
			
			return String.format(" AND date(o.creation_time) BETWEEN '%tF' AND '%tF'", dateFrom, dateTo);
		} catch (ParseException e) {
			throw new AppliancesRequestException("Incorrect date format!");
		}
	}

	@Transactional
	@Override
	public Order getById(int id) {
		Order order = null;
		
		try {
			order = jdbcTemplate.queryForObject(getById, (rs, rowNum) -> {
				return fetchOrder(rs);
			}, id);
		} catch (DataAccessException e) {
			// Nothing to do
		}
		
		if (order == null) return null;
		
		final List<Basket> baskets = jdbcTemplate.query(getAllFromBasket, (rs, rowNum) -> {
			return fetchBasket(rs);
		}, order.getId());
		
		order.setBaskets(baskets);
		return order;
	}

	@Transactional
	@Override
	public boolean delete(int id) {
		jdbcTemplate.update(deleteBaskets, id);
		return jdbcTemplate.update(delete, id) == 1;
	}

	@Override
	public boolean changeStatus(Order order) {
		if (order.getEmployee() == null) {
			return jdbcTemplate.update(changeStatus, order.getStatus().getId(), order.getId()) == 1;
		}
		return jdbcTemplate.update(changeStatusAndEmployee, order.getStatus().getId(), order.getEmployee().getId(), order.getId()) == 1;
	}
	
}