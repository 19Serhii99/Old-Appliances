package appliances.dao.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import appliances.dao.BrandDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Brand;
import appliances.models.Country;

@Transactional
public class BrandDAOImpl implements BrandDAO {
	
	private final static String collection = "brand";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public BrandDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}

	@Override
	public List<Brand> getAll() {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final List<Brand> brands = new LinkedList<>();
		
		final Bson lookup = Aggregates.lookup("country", "countryId", "_id", "country");
		final List<Bson> operations = Arrays.asList(lookup);
		
		final AggregateIterable<Document> results =  mongoCollection.aggregate(operations);
		results.forEach(document -> brands.add(getBrandFromDocument(document)));
		
		return brands;
	}
	
	@Override
	public boolean exists(Brand brand) {
		final Criteria criteria = where("name").is(brand.getName());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Brand.class);
	}

	@Transactional
	@Override
	public Brand create(Brand brand) {
		final Country country = checkBeforeCreation(brand);
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final int id = autoincrement.nextId(collection);
		
		final Document document = new Document("_id", id)
				.append("name", brand.getName())
				.append("countryId", country.getId());
		
		mongoCollection.insertOne(document);
		
		brand.setId(id);
		brand.setCountry(country);
		
		return brand;
	}
	
	private Country checkBeforeCreation(Brand brand) {
		if (exists(brand)) {
			throw new AppliancesRequestException("Brand with the specified name already exists!");
		}
		
		if (brand.getCountry() == null) {
			throw new AppliancesRequestException("Country must be present!");
		}
		
		if (brand.getCountry().getId() == 0) {
			throw new AppliancesRequestException("Country id must be present!");
		}
		
		final Country country = getCountryOfBrand(brand);
		
		if (country == null) {
			throw new AppliancesRequestException("Invalid country id!");
		}
		
		return country;
	}
	
	private Country getCountryOfBrand(Brand brand) {
		return mongoTemplate.findById(brand.getCountry().getId(), Country.class);
	}

	@Transactional
	@Override
	public boolean update(Brand brand) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		if (brand.getId() == 0) {
			throw new AppliancesRequestException("Brand id must be present!");
		}
		
		final Brand brandDb = getById(brand.getId());
		
		final Document update = new Document("name", brand.getName());
		
		if (brandDb == null) {
			throw new AppliancesRequestException("Invalid brand id!");
		}
		
		if (exists(brand)) {
			final Criteria criteria = where("name").is(brand.getName());
			final Query query = query(criteria);
			
			final Brand another = mongoTemplate.findOne(query, Brand.class);
			
			if (another != null && another.getId() != brand.getId()) {
				throw new AppliancesRequestException("Brand with the specified name already exists!");
			}
		}
		
		if (brand.getCountry().getId() == 0) {
			throw new AppliancesRequestException("Country id must be present!");
		}
		
		if (brand.getCountry().getId() != brandDb.getCountry().getId()) {
			final Country country = mongoTemplate.findById(brand.getCountry().getId(), Country.class);
			
			if (country == null) {
				throw new AppliancesRequestException("Invalid country id!");
			}
			
			update.append("countryId", country.getId());
		}
		
		final Bson filter = Filters.eq(brandDb.getId());
		final Bson updateStatement = new Document("$set", update);
		
		final UpdateResult result = mongoCollection.updateOne(filter, updateStatement);
		return result.getModifiedCount() == 1;
	}

	@Transactional
	@Override
	public boolean delete(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection("product");
		
		final Bson filter = Filters.eq("brandId", id);
		final Document product = mongoCollection.find(filter).first();
		
		if (product != null) {
			throw new AppliancesRequestException("There is a product that refers to the deleted object!");
		}
		
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, Brand.class);
		
		return result.getDeletedCount() == 1;
	}

	@Override
	public Brand getById(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(id);
		final Bson match = Aggregates.match(filter);
		
		final Bson lookup = Aggregates.lookup("country", "countryId", "_id", "country");
		final Bson limit = Aggregates.limit(1);
		
		final List<Bson> operations = Arrays.asList(match, lookup, limit);
		final Document document = mongoCollection.aggregate(operations).first();
		
		return getBrandFromDocument(document);
	}

	@Override
	public List<Brand> getAllByCategory(int categoryId) {
		final List<Integer> brandList = getBrandList(categoryId);
		
		final Criteria criteria = where("_id").in(brandList);
		final Query query = query(criteria);
		
		return mongoTemplate.find(query, Brand.class);
	}
	
	private List<Integer> getBrandList(int categoryId) {
		final List<Integer> brandList = new LinkedList<>();
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection("product");
		final Bson filter = Filters.eq("categoryId", categoryId);
		
		final FindIterable<Document> iter = mongoCollection.find(filter);
		
		iter.forEach(document -> {
			final int id = document.getInteger("brandId");
			
			boolean none = brandList.stream().noneMatch(element -> element == id);
			if (none) {
				brandList.add(id);
			}
		});
		
		return brandList;
	}
	
	private Brand getBrandFromDocument(Document document) {
		if (document == null) return null;
		
		final Brand brand = new Brand(document.getInteger("_id"));
		brand.setName(document.getString("name"));
		
		final List<Document> countries = document.getList("country", Document.class);
		
		if (countries.isEmpty()) {
			throw new AppliancesRequestException("An error has occured to get a country!");
		}
		
		final Document countryDocument = countries.get(0);
		final Country country = new Country(countryDocument.getInteger("_id"));
		
		country.setName(countryDocument.getString("name"));
		brand.setCountry(country);
		
		return brand;
	}
	
}