package appliances.dao.mongodb;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;

@Component
public class Autoincrement {
	
	private final MongoTemplate mongoTemplate;
	
	public Autoincrement(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public int nextId(String collection) {
		return getLastInsertedId(collection) + 1;
	}
	
	private int getLastInsertedId(String collection) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson sort = new Document("_id", -1);
		final Document document = mongoCollection.find().sort(sort).limit(1).first();
		
		if (document == null) return 0;
		
		final Integer orderId = document.getInteger("_id");
		
		return orderId;
	}
}