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

import appliances.dao.PositionDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Position;

public class PositionDAOImpl implements PositionDAO {
	
	private final String collection = "position";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public PositionDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}

	@Override
	public List<Position> getAll() {
		return mongoTemplate.findAll(Position.class);
	}

	@Override
	public Position create(Position position) {
		checkPosition(position);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final int id = autoincrement.nextId(collection);
		
		final Document document = new Document("_id", id)
				.append("name", position.getName());
		
		mongoCollection.insertOne(document);
		position.setId(id);
		
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
		final Criteria criteria = where("name").is(position.getName());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Position.class);
	}

	@Override
	public boolean update(int id, String name) {
		checkPosition(id, name);
		
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final Update update = new Update().set("name", name);
		final UpdateResult result = mongoTemplate.updateFirst(query, update, Position.class);
		
		return result.getModifiedCount() == 1;
	}

	@Override
	public boolean delete(int id) {
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, Position.class);
		
		return result.getDeletedCount() == 1;
	}

	@Override
	public Position getById(int id) {
		return mongoTemplate.findById(id, Position.class);
	}
	
}