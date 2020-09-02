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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import appliances.dao.CategoryDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Category;

@Transactional
public class CategoryDAOImpl implements CategoryDAO {
	
	private final static String collection = "category";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	public CategoryDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
	}

	@Override
	public List<Category> getAll() {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final List<Category> categories = new LinkedList<>();
		
		final Bson lookup = Aggregates.lookup(collection, "parentId", "_id", "parent");
		final List<Bson> aggregationList = Arrays.asList(lookup);
		final AggregateIterable<Document> iter = mongoCollection.aggregate(aggregationList);
		
		iter.forEach(document -> categories.add(getCategoryFromDocument(document)));
		
		return categories;
	}

	@Transactional
	@Override
	public Category create(Category category) {
		checkCategory(category);
		
		final int id = autoincrement.nextId(collection);
		final Document document = new Document("_id", id)
				.append("name", category.getName());
		
		final Category parent = findParentOfCategory(category);
		category.setParent(parent);
		
		if (parent != null) {
			final int parentId = parent.getId();
			document.append("parentId", parentId);
		}
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		mongoCollection.insertOne(document);
		category.setId(id);
		
		return category;
	}
	
	private void checkCategory(Category category) {
		final boolean present = exists(category);
		
		if (present) {
			throw new AppliancesRequestException("Category with the specified name already exists!");
		}
	}
	
	private Category findParentOfCategory(Category category) {
		if (!checkParentDataInCategory(category)) return null;
		
		final int parentId = category.getParent().getId();
		final Category parent = getById(parentId);
		
		checkParentData(parent);
		
		return parent;
	}
	
	private boolean checkParentDataInCategory(Category category) {
		if (category.getParent() == null) return false;
		
		if (category.getParent().getId() == 0) {
			throw new AppliancesRequestException("Parent id must be present!");
		}
		
		return true;
	}
	
	private void checkParentData(Category parent) {
		if (parent == null) {
			throw new AppliancesRequestException("Invalid parent id!");
		}
		
		if (parent.getParent() != null) {
			throw new AppliancesRequestException("You cannot create a new category as the third level!");
		}
	}

	@Override
	public boolean exists(Category category) {
		final Criteria criteria = where("name").is(category.getName());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Category.class);
	}

	@Transactional
	@Override
	public boolean update(Category category) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		if (category.getId() == 0) {
			throw new AppliancesRequestException("Invalid category id!");
		}
		
		checkCategory(category);
		
		final Category categoryDb = getById(category.getId());
		
		if (categoryDb == null) {
			throw new AppliancesRequestException("Invalid category id!");
		}
		
		final Document update = new Document("name", category.getName());
		
		if (category.getParent() != null && category.getParent().getId() != categoryDb.getParent().getId()) {
			final Category parent = getById(category.getParent().getId());
			
			if (parent == null) {
				throw new AppliancesRequestException("Invalid parent id!");
			}
			
			if (parent.getParent() != null) {
				throw new AppliancesRequestException("You cannot create a new category as the third level!");
			}
			
			if (parent.getId() == categoryDb.getId()) {
				throw new AppliancesRequestException("Category and its parent cannot be the same!");
			}
			
			update.append("parent", parent);
		}
		
		final Bson filter = Filters.eq(category.getId());
		final Bson updateBson = Updates.combine(update);
		
		final UpdateResult result = mongoCollection.updateOne(filter, updateBson);
		
		return result.getModifiedCount() == 1;
	}

	@Override
	public boolean delete(int id) {
		checkCategoryReference(id);
		
		final Criteria criteria = where("_id").is(id);
		final Query query = query(criteria);
		
		final DeleteResult result = mongoTemplate.remove(query, Category.class);
		
		return result.getDeletedCount() == 1;
	}
	
	private void checkCategoryReference(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection("product");
		final Bson filter = Filters.eq("categoryId", id);
		
		final Document document = mongoCollection.find(filter).first();
		
		if (document!= null) {
			throw new AppliancesRequestException("There is a product that refers to the deleted object!");
		}
	}

	@Override
	public Category getById(int id) {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		
		final Bson filter = Filters.eq(id);
		final Bson match = Aggregates.match(filter);
		
		final Bson lookup = Aggregates.lookup(collection, "parentId", "_id", "parent");
		final Bson limit = Aggregates.limit(1);
		
		final List<Bson> operationList = Arrays.asList(match, lookup, limit);
		final Document document = mongoCollection.aggregate(operationList).first();
		
		return getCategoryFromDocument(document);
	}
	
	
	private Category getCategoryFromDocument(Document document) {
		if (document == null) return null;
		
		final int categoryId = document.getInteger("_id");
		final String name = document.getString("name");
		final Category category = new Category(categoryId, name);
		
		final List<Document> parentDocuments = document.getList("parent", Document.class);
		
		if (!parentDocuments.isEmpty()) {
			final Document parentDocument = parentDocuments.get(0);
			final int parentId = parentDocument.getInteger("_id");
			
			final String parentName = parentDocument.getString("name");
			final Category parentCategory = new Category(parentId, parentName);
			
			category.setParent(parentCategory);
		}
		
		return category;
	}
	
}