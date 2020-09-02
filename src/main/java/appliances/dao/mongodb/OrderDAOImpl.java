package appliances.dao.mongodb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

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
	
	private final static String collection = "order";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	private final UserDAO userDAO;
	private final EmployeeDAO employeeDAO;
	private final StatusDAO statusDAO;
	private final ProductDAO productDAO;
	
	public OrderDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement, UserDAO userDAO, EmployeeDAO employeeDAO, StatusDAO statusDAO, ProductDAO productDAO) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
		
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
		final int orderId = autoincrement.nextId(collection);
		
		final Date creationTime = new Date();
		
		final Document document = new Document("_id", orderId)
				.append("userId", userId)
				.append("lastName", order.getLastName())
				.append("firstName", order.getFirstName())
				.append("middleName", order.getMiddleName())
				.append("phone", order.getPhone())
				.append("employeeId", null)
				.append("processing", false)
				.append("executed", false)
				.append("city", order.getCity())
				.append("postOffice", order.getPostOffice())
				.append("deliveryCost", order.getDeliveryCost())
				.append("canceled", false)
				.append("statusId", statusId)
				.append("total", order.getTotal())
				.append("basket", generateBasketDocuments(order))
				.append("creationTime", creationTime);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		mongoCollection.insertOne(document);
		
		return orderId;
	}
	
	public List<Document> generateBasketDocuments(Order order) {
		final List<Document> basketItems = new LinkedList<>();
		
		order.getBaskets().forEach(basket -> {
			final int productId = basket.getProduct().getId();
			
			final Document basketItemDocument = new Document("productId", productId)
				.append("price", basket.getPrice())
				.append("amount", basket.getAmount());
			
			basketItems.add(basketItemDocument);
		});
		
		return basketItems;
	}

	@Override
	public List<Order> getAll() {
		final MongoCollection<Document> orderCollection = mongoTemplate.getCollection(collection);
		
		final Integer statusId = getNewStatusId();
		
		if (statusId == null) {
			throw new AppliancesRequestException("Status is undefined!");
		}
		
		final Bson statusIdBson = Filters.eq("statusId", statusId);
		final FindIterable<Document> orders = orderCollection.find(statusIdBson);
		final List<Order> orderList = new LinkedList<>();
		
		orders.forEach(document -> {
			final Order order = getOrderFromDocument(document);
			orderList.add(order);
		});
		
		return orderList;
	}
	
	private Integer getNewStatusId() {
		final MongoCollection<Document> statusCollection = mongoTemplate.getCollection("status");
		
		final Bson statusBson = Filters.eq("name", "New");
		final Document statusDocument = statusCollection.find(statusBson).first();
		final Integer statusId = statusDocument.getInteger("_id");
		
		return statusId;
	}
	
	private Order getOrderFromDocument(Document document) {
		final Order order = new Order(document.getInteger("_id"));
		
		order.setLastName(document.getString("lastName"));
		order.setFirstName(document.getString("firstName"));
		order.setMiddleName(document.getString("middleName"));
		
		order.setPhone(document.getString("phone"));
		
		order.setCity(document.getString("city"));
		order.setPostOffice(document.getInteger("postOffice"));
		order.setDeliveryCost((float)(double) document.getDouble("deliveryCost"));
		order.setTotal((float)(double) document.getDouble("total"));
		
		order.setProcessing(document.getBoolean("processing"));
		order.setExecuted(document.getBoolean("executed"));
		order.setCanceled(document.getBoolean("canceled"));
		order.setCreationTime(document.getDate("creationTime"));
		
		final Integer userId = document.getInteger("userId");
		if (userId != null) {
			final User user = userDAO.getById(userId);
			order.setUser(user);
		}
		
		final Integer employeeId = document.getInteger("employeeId");
		if (employeeId != null) {
			final Employee employee = employeeDAO.getById(employeeId);
			order.setEmployee(employee);
		}
		
		final Integer statusId = document.getInteger("statusId");
		final Status status = statusDAO.getById(statusId);
		order.setStatus(status);
		
		final List<Document> baskets = document.getList("basket", Document.class);
		final List<Basket> basketList = new LinkedList<>();
		baskets.forEach(basket -> {
			final Basket basketItem = new Basket();
			
			final int productId = basket.getInteger("productId");
			final Product product = productDAO.getById(productId);
			
			basketItem.setProduct(product);
			basketItem.setPrice((float)(double) basket.getDouble("price"));
			basketItem.setAmount(basket.getInteger("amount"));
			
			basketList.add(basketItem);
		});
		
		order.setBaskets(basketList);
		
		return order;
	}

	@Override
	public List<Order> getAllByFilter(Map<String, List<String>> filter, int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final List<Bson> operations = new LinkedList<Bson>();
		
		filter.forEach((key, values) -> {
			final Bson bson = handleFilter(key, values, id);
			if (bson != null) operations.add(bson);
		});
		
		final boolean hasStatus = filter.entrySet()
			.stream()
			.filter(map -> "status".equals(map.getKey()))
			.findFirst()
			.isPresent();
		
		if (!hasStatus) {
			final Integer statusId = getNewStatusId();
			
			final Bson statusBson = Filters.eq("statusId", statusId);
			final Bson bson = Aggregates.match(statusBson);
			
			operations.add(bson);
		}
		
		final AggregateIterable<Document> iter = mongoCollection.aggregate(operations);
		final List<Order> orders = new LinkedList<>();
		
		iter.forEach(document -> {
			final Order order = getOrderFromDocument(document);
			orders.add(order);
		});
		
		return orders;
	}
	
	private Bson handleFilter(String key, List<String> values, int id) {
		if (key.equals("status")) {
			return handleStatus(values, id);
		}
		
		if (key.equals("date")) {
			return handleDate(values);
		}
		
		return null;
	}
	
	private Bson handleStatus(List<String> values, int id) {
		final int statusId = Integer.parseInt(values.get(0));
		final Bson statusBson = Filters.eq("statusId", statusId);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection("status");
		final Bson findStatusBson = Filters.eq(statusId);
		
		final Document statusDocument = mongoCollection.find(findStatusBson).first();
		final String statusName = statusDocument.getString("name");
		
		if ("New".equals(statusName)) {
			return Aggregates.match(statusBson);
		}
		
		final Bson employeeBson = Filters.eq("employeeId", id);
		final Bson filterAnd = Filters.and(statusBson, employeeBson);
		
		return Aggregates.match(filterAnd);
	}
	
	private Bson handleDate(List<String> values) {
		try {
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Calendar calendar = Calendar.getInstance();
			
			calendar.setTime(formatter.parse(values.get(1)));
			calendar.add(Calendar.DATE, 1);
			
			final Date dateFrom = formatter.parse(values.get(0));
			final Date dateTo = calendar.getTime();
			
			final Bson from = Filters.gte("creationTime", dateFrom);
			final Bson to = Filters.lte("creationTime", dateTo);
			
			final Bson and = Filters.and(from, to);
			
			return Aggregates.match(and);
		} catch (ParseException e) {
			throw new AppliancesRequestException("Incorrect date format!");
		}
	}

	@Override
	public Order getById(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(id);
		final Document document = mongoCollection.find(filter).first();
		
		return getOrderFromDocument(document);
	}

	@Override
	public boolean delete(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(id);
		final DeleteResult result = mongoCollection.deleteOne(filter);
		
		return result.getDeletedCount() == 1;
	}

	@Override
	public boolean changeStatus(Order order) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(order.getId());
		final Bson updateStatus = Updates.set("statusId", order.getStatus().getId());
		
		final Bson updateEmployee = Updates.set("employeeId", order.getEmployee().getId());
		final Bson updateStatement = Updates.combine(updateStatus, updateEmployee);
		
		final UpdateResult result = mongoCollection.updateOne(filter, updateStatement);
		
		return result.getModifiedCount() == 1;
	}
	
}