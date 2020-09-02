package appliances.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import appliances.models.Position;
import appliances.services.PositionService;

@RequestMapping("api/v1/positions")
@RestController
public class PositionController {
	
	private final PositionService positionService;

	@Autowired
	public PositionController(PositionService positionService) {
		this.positionService = positionService;
	}
	
	@GetMapping
	public List<Position> getPositions() {
		return positionService.getAll();
	}
	
	@PostMapping
	public Position createPosition(@RequestBody Position position) {
		return positionService.create(position);
	}
	
	@PutMapping(path = "{id}/{name}")
	public void updatePosition(@PathVariable int id, @PathVariable String name) {
		positionService.update(id, name);
	}
	
	@DeleteMapping(path = "{id}")
	public void deletePosition(@PathVariable int id) {
		positionService.delete(id);
	}
	
}