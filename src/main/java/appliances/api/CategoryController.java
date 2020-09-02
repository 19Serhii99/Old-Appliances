package appliances.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import appliances.models.Category;
import appliances.services.CategoryService;

@RequestMapping("api/v1/categories")
@RestController
public class CategoryController {
	
	private final CategoryService categoryService;

	@Autowired
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@GetMapping
	public List<Category> getCategories() {
		return categoryService.getAll();
	}
	
	@GetMapping(path = "{id}")
	public Category getCategoryById(@PathVariable int id) {
		return categoryService.getById(id);
	}
	
	@PostMapping
	public Category createCategory(@RequestBody Category category) {
		return categoryService.create(category);
	}
	
	@PostMapping(path = "update")
	public void updateCategory(@RequestBody Category category) {
		categoryService.update(category);
	}
	
	@DeleteMapping(path = "{id}")
	public void delete(@PathVariable int id) {
		categoryService.delete(id);
	}
	
}