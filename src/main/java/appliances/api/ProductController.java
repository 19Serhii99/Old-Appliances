package appliances.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import appliances.exceptions.FieldErrorType;
import appliances.exceptions.FieldExceptionObject;
import appliances.exceptions.FormRequestException;
import appliances.models.Product;
import appliances.services.ProductService;

@RequestMapping("api/v1/products")
@RestController
public class ProductController {
	
	private final ProductService productService;
	
	@Autowired
	public ProductController(ProductService productService) {
		this.productService = productService;
	}
	
	@GetMapping(path = {"{categoryId}", "{categoryId}/{filter}"})
	public List<Product> getProducts(@PathVariable int categoryId, @MatrixVariable Map<String, List<String>> filter) {
		if (filter.isEmpty()) {
			return productService.getAll(categoryId);
		}
		return productService.getAllByFilter(categoryId, filter);
	}
	
	@GetMapping(path = "search")
	public List<Product> search(@RequestParam(defaultValue = " ") String text) {
		return productService.search(text);
	}
	
	@GetMapping(path = "{categoryId}/{filter}/min-price")
	public float getMinPrice(@PathVariable int categoryId, @MatrixVariable Map<String, List<String>> filter) {
		return productService.getMinPrice(categoryId, filter);
	}
	
	@GetMapping(path = "{categoryId}/min-price")
	public float getMinPriceWithoutFilter(@PathVariable int categoryId) {
		return productService.getMinPrice(categoryId, null);
	}
	
	@GetMapping(path = "{categoryId}/{filter}/max-price")
	public float getMaxPrice(@PathVariable int categoryId, @MatrixVariable Map<String, List<String>> filter) {
		return productService.getMaxPrice(categoryId, filter);
	}
	
	@GetMapping(path = "{categoryId}/max-price")
	public float getMaxPriceWithoutFilter(@PathVariable int categoryId) {
		return productService.getMaxPrice(categoryId, null);
	}
	
	@GetMapping(path = "details/{id}")
	public Product getProductById(@PathVariable int id) {
		return productService.getById(id);
	}
	
	@PutMapping(path = "{id}/{visible}")
	public void setProductVisibility(@PathVariable int id, @PathVariable boolean visible) {
		if (visible) productService.show(id);
		else productService.hide(id);
	}
	
	@PostMapping
	public void createProduct(@Valid @RequestBody Product product, BindingResult br) {
		if (br.hasErrors()) generateException(br);
		
		productService.create(product);
	}
	
	private void generateException(BindingResult br) {
		final List<FieldError> fieldErrors = br.getFieldErrors();
		final List<FieldExceptionObject> errors = new LinkedList<>();
		
		fieldErrors.forEach(fieldError -> {
			FieldErrorType fieldErrorType = FieldErrorType.NONE;
			
			switch (fieldError.getField()) {
				case "name":
					fieldErrorType = FieldErrorType.NAME;
					break;
				case "categoryId":
					fieldErrorType = FieldErrorType.CATEGORY_ID;
					break;
				case "brandId":
					fieldErrorType = FieldErrorType.BRAND_ID;
					break;
			}
			
			final String message = fieldError.getDefaultMessage();
			final FieldExceptionObject object = new FieldExceptionObject(message, fieldErrorType);
			
			errors.add(object);
		});
		
		throw new FormRequestException(errors);
	}
	
	@PostMapping(path = "update")
	public void updateProduct(@RequestBody Product product) {
		productService.update(product);
	}
	
	@DeleteMapping(path = "{id}")
	public void deleteProduct(@PathVariable int id) {
		productService.delete(id);
	}
	
	/** Use it only when it is necessary (for filling a database) **/
	@GetMapping(path = "fill-random-data")
	public void fillProducts() {
		productService.fillProducts();
	}
	
}