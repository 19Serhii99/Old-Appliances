package appliances.dao.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import appliances.dao.BrandDAO;
import appliances.dao.CategoryDAO;
import appliances.dao.ProductDAO;
import appliances.exceptions.AppliancesRequestException;
import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Brand;
import appliances.models.Category;
import appliances.models.Product;

public class ProductDAOImpl implements ProductDAO {
	
	private final static String collection = "product";
	
	private final MongoTemplate mongoTemplate;
	private final Autoincrement autoincrement;
	
	private final CategoryDAO categoryDAO;
	private final BrandDAO brandDAO;
	
	private final static List<AggregationOperation> operations =  Arrays.asList(
			Aggregation.lookup("category", "categoryId", "_id", "category"),
			Aggregation.unwind("$category"),
			Aggregation.lookup("brand", "brandId", "_id", "brand"),
			Aggregation.unwind("$brand"),
			Aggregation.lookup("country", "brand.countryId", "_id", "brand.country"),
			Aggregation.unwind("$brand.country")
	);
	
	public ProductDAOImpl(MongoTemplate mongoTemplate, Autoincrement autoincrement, CategoryDAO categoryDAO, BrandDAO brandDAO) {
		this.mongoTemplate = mongoTemplate;
		this.autoincrement = autoincrement;
		this.categoryDAO = categoryDAO;
		this.brandDAO = brandDAO;
		
	}
	
	@Override
	public List<Product> getAll(int categoryId) {
		final List<AggregationOperation> opts = new LinkedList<>(operations);
		
		final Criteria categoryCriteria = where("categoryId").is(categoryId);
		final Criteria hiddenCriteria = where("hidden").is(false);
		
		final MatchOperation categoryOperation = Aggregation.match(categoryCriteria);
		final MatchOperation hiddenOperation = Aggregation.match(hiddenCriteria);
		
		opts.add(categoryOperation);
		opts.add(hiddenOperation);
		
		final Aggregation aggregation = Aggregation.newAggregation(opts);
		final AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, Product.class, Product.class);
		
		return results.getMappedResults();
	}

	@Override
	public boolean exists(Product product) {
		final Criteria criteria = where("name").is(product.getName());
		final Query query = query(criteria);
		
		return mongoTemplate.exists(query, Product.class);
	}
	
	private void checkProduct(Product product) {
		final boolean present = exists(product);
		
		if (present) {
			final FieldExceptionObject error = new FieldExceptionObject("Product with the specified name already exists!", FieldErrorType.NAME);
			throw new FormRequestException(error);
		}
	}
	
	private void checkCategory(Category category) {
		if (category == null) {
			throw new AppliancesRequestException("Invalid category id!");
		}
	}
	
	private void checkBrand(Brand brand) {
		if (brand == null) {
			throw new AppliancesRequestException("Invalid brand id!");
		}
	}

	@Override
	public Product create(Product product) {
		checkProduct(product);
		
		final Category category = categoryDAO.getById(product.getCategory().getId());
		checkCategory(category);
		
		final Brand brand = brandDAO.getById(product.getBrand().getId());
		checkBrand(brand);
		
		final int id = autoincrement.nextId(collection);
		final int categoryId = product.getCategory().getId();
		final int brandId = product.getBrand().getId();
		
		final Document document = new Document("_id", id)
				.append("name", product.getName())
				.append("price", product.getPrice())
				.append("amount", product.getAmount())
				.append("width", product.getWidth())
				.append("height", product.getHeight())
				.append("depth", product.getDepth())
				.append("weight", product.getWeight())
				.append("hidden", false)
				.append("categoryId", categoryId)
				.append("brandId", brandId);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		mongoCollection.insertOne(document);
		
		product.setId(id);
		product.setCategory(category);
		product.setBrand(brand);
		
		return product;
	}

	@Override
	public float getMinPrice(int categoryId, Map<String, List<String>> filter) {
		return getPriceValue(categoryId, filter, Sort.Direction.ASC);
	}

	@Override
	public float getMaxPrice(int categoryId, Map<String, List<String>> filter) {
		return getPriceValue(categoryId, filter, Sort.Direction.DESC);
	}

	private float getPriceValue(int categoryId, Map<String, List<String>> filter, Sort.Direction direction) {
		final List<Product> filteredProducts = filter == null ? getAll(categoryId) : getAllByFilter(categoryId, filter);
		if (filteredProducts.isEmpty()) return 0.0f;
		
		final Map<Float, List<Product>> groupedProducts = filteredProducts.stream().collect(Collectors.groupingBy(Product::getPrice));
		
		Optional<Entry<Float, List<Product>>> optional = null;
		
		if (direction == Sort.Direction.ASC) {
			optional = groupedProducts.entrySet().stream().min(Comparator.comparing(Map.Entry::getKey));
		} else {
			optional = groupedProducts.entrySet().stream().max(Comparator.comparing(Map.Entry::getKey));
		}
	
		return optional.get().getKey();
	}

	@Override
	public Product getById(int productId) {
		final List<AggregationOperation> opts = new LinkedList<>(operations);
		
		final Criteria criteria = where("_id").is(productId);
		final MatchOperation matchoperation = Aggregation.match(criteria);
		final LimitOperation limitOperation = Aggregation.limit(1);
		
		opts.add(matchoperation);
		opts.add(limitOperation);
		
		final Aggregation aggregation = Aggregation.newAggregation(opts);
		final AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, Product.class, Product.class);
		
		return results.getUniqueMappedResult();
	}

	@Override
	public boolean update(Product product) {
		final Product productDb = getById(product.getId());
		
		if (productDb == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
		
		if (productDb.getName().equals(product.getName()) && productDb.getId() != product.getId()) {
			throw new AppliancesRequestException("Product with the specified name already exists!");
		}
		
		final Category category = categoryDAO.getById(product.getCategory().getId());
		checkCategory(category);
		
		final Brand brand = brandDAO.getById(product.getBrand().getId());
		checkBrand(brand);
		
		if (product.getId() == 0) {
			throw new AppliancesRequestException("Product id must be present!");
		}
		
		if (getById(product.getId()) == null) {
			throw new AppliancesRequestException("Invalid product id!");
		}
		
		final int categoryId = product.getCategory().getId();
		final int brandId = product.getBrand().getId();
		
		final Document update = new Document("name", product.getName())
				.append("price", product.getPrice())
				.append("amount", product.getAmount())
				.append("width", product.getWidth())
				.append("height", product.getHeight())
				.append("depth", product.getDepth())
				.append("weight", product.getWeight())
				.append("hidden", product.isHidden())
				.append("categoryId", categoryId)
				.append("brandId", brandId);
		
		final Bson query = Filters.eq(product.getId());
		final Bson updateStatement = new Document("$set", update);
		
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final UpdateResult result = mongoCollection.updateOne(query, updateStatement);
		
		return result.getModifiedCount() > 0;
	}

	@Override
	public List<Product> getAllByFilter(int categoryId, Map<String, List<String>> filter) {
		final List<AggregationOperation> opts = formFilter(categoryId, filter);
		
		final Aggregation aggregation = Aggregation.newAggregation(opts);
		final AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, Product.class, Product.class);
		
		return results.getMappedResults();
	}
	
	private List<AggregationOperation> formFilter(int categoryId, Map<String, List<String>> filter) {
		final List<AggregationOperation> opts = new LinkedList<>(operations);
		
		final Criteria criteria = where("categoryId").is(categoryId);
		final MatchOperation operation = Aggregation.match(criteria);
		
		opts.add(operation);
		filter.entrySet().forEach(item -> checkFilterConditions(opts, item));
		
		return opts;
	}
	
	private void checkFilterConditions(List<AggregationOperation> opts, Entry<String, List<String>> conditions) {
		final String condition = conditions.getKey();
		final List<String> value = conditions.getValue();
		
		if (condition.equals("brands")) {
			makeBrandsFilter(opts, value);
			return;
		}
		
		if (condition.equals("price")) {
			makePriceFilter(opts, value);
			return;
		}
		
		if (condition.equals("visibility")) {
			makeVisibilityFilter(opts, value.get(0));
			return;
		}
		
		if (condition.equals("sort")) {
			makeSortFilter(opts, value.get(0));
			return;
		}
	}
	
	private void makeBrandsFilter(List<AggregationOperation> opts, List<String> values) {
		final List<Integer> brands = new LinkedList<>();
		values.forEach(brand -> brands.add(Integer.parseInt(brand)));
		
		final Criteria criteria = where("brandId").in(brands);
		final MatchOperation operation = Aggregation.match(criteria);
		
		opts.add(operation);
	}
	
	private void makePriceFilter(List<AggregationOperation> opts, List<String> values) {
		final float minPrice = Float.parseFloat(values.get(0));
		final float maxPrice = Float.parseFloat(values.get(1));
		
		final Criteria criteria = where("price").gte(minPrice).lte(maxPrice);
		final MatchOperation operation = Aggregation.match(criteria);
		
		opts.add(operation);
	}
	
	private void makeVisibilityFilter(List<AggregationOperation> opts, String value) {
		final boolean visible = value.equals("1");
		
		final Criteria criteria = where("hidden").is(visible);
		final MatchOperation operation = Aggregation.match(criteria);
		
		opts.add(operation);
	}
	
	private void makeSortFilter(List<AggregationOperation> opts, String sort) {
		if (sort.equals("expensive-cheap")) {
			opts.add(Aggregation.sort(Direction.DESC, "price"));
			return;
		}
		
		if (sort.equals("cheap-expensive")) {
			opts.add(Aggregation.sort(Direction.ASC, "price"));
			return;
		}
		
		if (sort.equals("end-start")) {
			opts.add(Aggregation.sort(Direction.ASC, "id"));
			return;
		}
		
		if (sort.equals("start-end")) {
			opts.add(Aggregation.sort(Direction.DESC, "id"));
			return;
		}
	}
	
	@Override
	public List<Product> search(String text) {
		final List<AggregationOperation> opts = new LinkedList<>(operations);
		
		final String expression = ".*" + text + ".*";
		
		final Criteria criteria1 = where("name").regex(expression, "i");
		final Criteria criteria2 = where("brand.name").regex(expression, "i");
		
		final Criteria criteria = new Criteria().orOperator(criteria1, criteria2);
		
		final MatchOperation match = Aggregation.match(criteria);
		opts.add(match);
		
		final Aggregation aggregation = Aggregation.newAggregation(opts);
		final AggregationResults<Product> results = mongoTemplate.aggregate(aggregation, Product.class, Product.class);
		
		return results.getMappedResults();
	}
	
	@Override
	public boolean hide(int id) {
		final Query query = query(where("_id").is(id));
		final Update updateStatement = Update.update("hidden", true);
		
		final UpdateResult result = mongoTemplate.updateFirst(query, updateStatement, Product.class);
		
		return result.getModifiedCount() > 0;
	}

	@Override
	public boolean delete(int id) {
		final Query query = query(where("_id").is(id));
		final DeleteResult result = mongoTemplate.remove(query, Product.class);
		
		return result.getDeletedCount() > 0;
	}

	@Override
	public boolean show(int id) {
		final Query query = query(where("_id").is(id));
		final Update updateStatement = Update.update("hidden", false);
		
		final UpdateResult result = mongoTemplate.updateFirst(query, updateStatement, Product.class);
		
		return result.getModifiedCount() > 0;
	}

	@Override
	public void fillRandomData() {
		final MongoCollection<Document> mongoCollection = mongoTemplate.getCollection(collection);
		final MongoCollection<Document> brandCollection = mongoTemplate.getCollection("brand");
		final MongoCollection<Document> categoryCollection = mongoTemplate.getCollection("category");
		
		final List<Integer> brands = new ArrayList<>();
		final List<Integer> categories = new ArrayList<>();
		
		brandCollection.find().forEach(document -> brands.add(document.getInteger("_id")));
		categoryCollection.find().forEach(document -> categories.add(document.getInteger("_id")));
		
		final List<Document> documents = new LinkedList<>();
		
		final int id = autoincrement.nextId(collection);
		
		for (int i = 0; i < 500; i++) {
			final Document document = new Document("_id", id + i)
					.append("name", "Product " + (id + i))
					.append("price", random(100.0f, 10000.0f))
					.append("amount", random(1, 50))
					.append("width", random(1.0f, 200.0f))
					.append("height", random(1.0f, 200.0f))
					.append("depth", random(1.0f, 200.0f))
					.append("weight", random(0.1f, 100.0f))
					.append("hidden", false)
					.append("categoryId", categories.get(random(0, categories.size() - 1)))
					.append("brandId", brands.get(random(0, brands.size() - 1)));
			documents.add(document);
		}
		
		mongoCollection.insertMany(documents);
	}
	
	private int random(int minValue, int maxValue) {
		final Random random = new Random();
		return random.nextInt(maxValue - minValue + 1) + minValue;
	}
	
	private float random(float minValue, float maxValue) {
		final Random random = new Random();
		return random.nextFloat() * (maxValue - minValue) + minValue;
	}
	
}