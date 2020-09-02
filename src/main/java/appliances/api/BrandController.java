package appliances.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import appliances.config.DAOType;
import appliances.config.DatabaseFactory;
import appliances.exceptions.AppliancesRequestException;
import appliances.models.Brand;
import appliances.services.BrandService;

@RequestMapping("api/v1/brands")
@RestController
public class BrandController {
	
	private final BrandService brandService;
	
	@Autowired
	public BrandController(BrandService brandService) {
		this.brandService = brandService;
	}
	
	@GetMapping
	public List<Brand> getBrands() {
		DatabaseFactory.getInstance().getFactory(DAOType.MongoDB);
		
		return brandService.getAll();
	}
	
	@GetMapping(path = "{id}")
	public List<Brand> getBrandsByCategory(@PathVariable int id) {
		return brandService.getAllByCategory(id);
	}
	
	@PostMapping
	public Brand createBrand(@Valid @RequestBody Brand brand, BindingResult br) {
		if (br.hasErrors()) {
			throw new AppliancesRequestException(br.getFieldError().getDefaultMessage());
		}
		return brandService.create(brand);
	}
	
	@PostMapping(path = "/update")
	public void updateBrand(@RequestBody Brand brand) {
		brandService.update(brand);
	}
	
	@DeleteMapping(path = "{id}")
	public void deleteBrand(@PathVariable int id) {
		brandService.delete(id);
	}
	
}