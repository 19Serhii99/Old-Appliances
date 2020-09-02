package appliances.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import appliances.exceptions.AppliancesRequestException;
import appliances.models.Country;
import appliances.services.CountryService;

@RequestMapping("api/v1/countries")
@RestController
public class CountryController {
	
	private final CountryService countryService;
	
	@Autowired
	public CountryController(CountryService countryService) {
		this.countryService = countryService;
	}
	
	@GetMapping
	public List<Country> getCountries() {
		return countryService.getAll();
	}
	
	@PostMapping
	public Country createCountry(@Valid @RequestBody Country country, BindingResult br) {
		if (br.hasErrors()) {
			throw new AppliancesRequestException(br.getFieldError().getDefaultMessage());
		}
		return countryService.create(country);
	}
	
	@PutMapping(path = "{id}/{name}")
	public void updateCountry(@PathVariable int id, @PathVariable String name) {
		countryService.update(id, name);
	}
	
	@DeleteMapping(path = "{id}")
	public void deleteCountry(@PathVariable int id) {
		countryService.delete(id);
	}
	
}