package appliances.dao.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import appliances.dao.StatusDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Status;

public class StatusDAOImpl implements StatusDAO {
	
	private final String collection = "status";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public StatusDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}

	@Override
	public List<Status> getAll() {
		return mongoTemplate.findAll(Status.class);
	}

	@Override
	public Status create(Status status) {
		checkStatus(status);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final int id = autoincrement.nextId(collection);
		
		final Document document = new Document("_id", id)
				.append("name", status.getName());
		
		mongoCollection.insertOne(document);
		status.setId(id);
		
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
		final Criteria criteria = where("name").is(status.getName());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Status.class);
	}

	@Override
	public boolean update(int id, String name) {
		checkStatus(id, name);
		
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final Update update = new Update().set("name", name);
		final UpdateResult result = mongoTemplate.updateFirst(query, update, Status.class);
		
		return result.getModifiedCount() > 0;
	}

	@Override
	public boolean delete(int id) {
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, Status.class);
		
		return result.getDeletedCount() > 0;
	}

	@Override
	public Status getById(int id) {
		return mongoTemplate.findById(id, Status.class);
	}

	@Override
	public Status getByName(String name) {
		final Criteria criteria = where("name").is(name);
		final Query query = query(criteria);
		
		return mongoTemplate.findOne(query, Status.class);
	}
	
}