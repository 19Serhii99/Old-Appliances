package appliances.dao.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import appliances.dao.EmployeeDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Employee;
import appliances.models.Position;

@Transactional
public class EmployeeDAOImpl implements EmployeeDAO {
	
	private final static String collection = "employee";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public EmployeeDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}
	
	@Transactional
	@Override
	public Employee create(Employee employee) {
		checkEmployee(employee);
		
		final Position position = mongoTemplate.findById(employee.getPosition().getId(), Position.class);
		checkPosition(position);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final int id = autoincrement.nextId(collection);
		
		final Document document = new Document("_id", id)
				.append("lastName", employee.getLastName())
				.append("firstName", employee.getFirstName())
				.append("middleName", employee.getMiddleName())
				.append("phone", employee.getPhone())
				.append("email", employee.getEmail())
				.append("password", employee.getPassword())
				.append("startDate", employee.getStartDate())
				.append("positionId", position.getId());
		
		mongoCollection.insertOne(document);
		
		employee.setId(id);
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
		final Criteria criteria = where("email").is(employee.getEmail());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Employee.class);
	}
	
	@Override
	public Employee signIn(String email, String password) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson emailFilter = Filters.eq("email", email);
		final Bson passwordFilter = Filters.eq("password", password);
		
		final Bson matchEmail = Aggregates.match(emailFilter);
		final Bson matchPassword = Aggregates.match(passwordFilter);
		
		final Bson lookup = Aggregates.lookup("position", "positionId", "_id", "position");
		final Bson limit = Aggregates.limit(1);
		
		final List<Bson> operations = Arrays.asList(matchEmail, matchPassword, lookup, limit);
		final Document document = mongoCollection.aggregate(operations).first();
		
		return getEmployeeFromDocument(document);
	}
	
	private Employee getEmployeeFromDocument(Document document) {
		if (document == null) return null;
		
		final Employee employee = new Employee(document.getInteger("_id"));
		
		employee.setLastName(document.getString("lastName"));
		employee.setFirstName(document.getString("firstName"));
		employee.setMiddleName(document.getString("middleName"));
		employee.setPhone(document.getString("phone"));
		employee.setEmail(document.getString("email"));
		employee.setStartDate(document.getDate("startDate"));
		
		final List<Document> positions = document.getList("position", Document.class);
		
		if (positions.isEmpty()) {
			throw new AppliancesRequestException("Invalid position id");
		}
		
		final Document positionDocument = positions.get(0);
		final Position position = new Position(positionDocument.getInteger("_id"));
		
		position.setName(positionDocument.getString("name"));
		employee.setPosition(position);
		
		return employee;
	}

	@Override
	public boolean update(Employee employee) {
		if (exists(employee)) {
			final Criteria criteria = where("email").is(employee.getEmail());
			final Query query = query(criteria);
			
			final Employee another = mongoTemplate.findOne(query, Employee.class);
			
			if (another != null && employee.getId() != another.getId()) {
				throw new AppliancesRequestException("Employee with the specified email already exists!");
			}
		}
		
		final Position position = mongoTemplate.findById(employee.getPosition().getId(), Position.class);
		
		if (position == null) {
			throw new AppliancesRequestException("Invalid position id!");
		}
		
		final Document update = new Document("lastName", employee.getLastName())
				.append("firstName", employee.getFirstName())
				.append("middleName", employee.getMiddleName())
				.append("phone", employee.getPhone())
				.append("email", employee.getEmail());
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(employee.getId());
		final Bson updateStatement = new Document("$set", update);
		
		final UpdateResult result = mongoCollection.updateOne(filter, updateStatement);
		
		return result.getModifiedCount() > 0;
	}

	@Override
	public Employee getById(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(id);
		final Bson match = Aggregates.match(filter);
		
		final Bson lookup = Aggregates.lookup("position", "positionId", "_id", "position");
		final Bson limit = Aggregates.limit(1);
		
		final List<Bson> operations = Arrays.asList(match, lookup, limit);
		final Document document = mongoCollection.aggregate(operations).first();
		
		return getEmployeeFromDocument(document);
	}

	@Override
	public boolean delete(int id) {
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, Employee.class);
		
		return result.getDeletedCount() > 0;
	}

	@Override
	public boolean updatePassword(int id, String password) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final Bson document = new Document("password", password);
		
		final Bson filter = Filters.eq(id);
		final Bson updateStatement = new Document("$set", document);
		
		mongoCollection.findOneAndUpdate(filter, updateStatement);
		
		return true;
	}
	
}