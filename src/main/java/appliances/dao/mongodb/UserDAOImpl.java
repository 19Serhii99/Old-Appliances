package appliances.dao.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import appliances.dao.UserDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.User;

@Transactional
public class UserDAOImpl implements UserDAO {
	
	private final String collection = "user";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public UserDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}

	@Override
	public boolean create(User user) {
		checkUser(user);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final int id = autoincrement.nextId(collection);
		
		final Document document = new Document("_id", id)
				.append("lastName", user.getLastName())
				.append("firstName", user.getFirstName())
				.append("middleName", user.getMiddleName())
				.append("phone", user.getPhone())
				.append("email", user.getEmail())
				.append("password", user.getPassword())
				.append("city", user.getCity())
				.append("birthday", user.getBirthday());
		
		mongoCollection.insertOne(document);

		return true;
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
		final Criteria criteria = where("email").is(user.getEmail());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, User.class);
	}

	@Override
	public User signIn(String email, String password) {
		final Criteria criteria = where("email").is(email).and("password").is(password);
		final Query query = query(criteria);
		
		return mongoTemplate.findOne(query, User.class);
	}

	@Override
	public User getById(int id) {
		return mongoTemplate.findById(id, User.class);
	}

	@Override
	public boolean update(User user) {
		if (exists(user)) {
			final Criteria criteria = where("email").is(user.getEmail());
			final Query query = query(criteria);
			
			final User another = mongoTemplate.findOne(query, User.class);
			
			if (another != null && user.getId() != another.getId()) {
				throw new AppliancesRequestException("User with the specified email already exists!");
			}
		}
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final Bson document = new Document("lastName", user.getLastName())
				.append("firstName", user.getFirstName())
				.append("middleName", user.getMiddleName())
				.append("phone", user.getPhone())
				.append("email", user.getEmail())
				.append("city", user.getCity())
				.append("birthday", user.getBirthday());
		
		final Bson filter = Filters.eq(user.getId());
		final Bson updateStatement = new Document("$set", document);
		
		mongoCollection.findOneAndUpdate(filter, updateStatement);
		
		return true;
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

	@Override
	public boolean delete(int id) {
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, User.class);
		
		return result.getDeletedCount() > 0;
	}

}