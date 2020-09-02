package appliances.dao.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import appliances.dao.CountryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Country;

@Transactional
public class CountryDAOImpl implements CountryDAO {
	
	private final String collection = "country";
	
	public MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public CountryDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}

	@Override
	public List<Country> getAll() {
		return mongoTemplate.findAll(Country.class);
	}

	@Override
	public boolean exists(Country country) {
		final Criteria criteria = where("name").is(country.getName());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Country.class);
	}

	@Override
	public Country create(Country country) {
		checkCountry(country);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final int id = autoincrement.nextId(collection);
		
		final Document document = new Document("_id", id)
				.append("name", country.getName());
		
		mongoCollection.insertOne(document);
		country.setId(id);
		
		return country;
	}
	
	private void checkCountry(Country country) {
		final boolean present = exists(country);
		
		if (present) {
			throw new AppliancesRequestException("Country with the specified name already exists!");
		}
	}
	
	private void checkCountry(int id, String name) {
		final Country country = new Country(id, name);
		checkCountry(country);
	}

	@Override
	public Country getById(int id) {
		return mongoTemplate.findById(id, Country.class);
	}

	@Override
	public boolean updateCountry(int id, String name) {
		checkCountry(id, name);
		
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final Update update = update("name", name);
		final UpdateResult result = mongoTemplate.updateFirst(query, update, Country.class);
		
		return result.getModifiedCount() == 1;
	}
	
	private void checkCountryReference(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection("brand");
		
		final Bson filter = Filters.eq("countryId", id);
		final Document document = mongoCollection.find(filter).first();
		
		if (document != null) {
			throw new AppliancesRequestException("There is a brand that refers to the deleted object!");
		}
	}

	@Override
	public boolean delete(int id) {
		checkCountryReference(id);
		
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, Country.class);
		
		return result.getDeletedCount() == 1;
	}
	
}