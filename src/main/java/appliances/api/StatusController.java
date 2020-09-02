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

import appliances.models.Status;
import appliances.services.StatusService;

@RequestMapping("api/v1/statuses")
@RestController
public class StatusController {
	
	private final StatusService statusService;
	
	@Autowired
	public StatusController(StatusService statusService) {
		this.statusService = statusService;
	}
	
	@GetMapping
	public List<Status> getPositions() {
		return statusService.getAll();
	}
	
	@PostMapping
	public Status createStatus(@RequestBody Status status) {
		return statusService.create(status);
	}
	
	@PutMapping(path = "{id}/{name}")
	public void updateStatus(@PathVariable int id, @PathVariable String name) {
		statusService.update(id, name);
	}
	
	@DeleteMapping(path = "{id}")
	public void deleteStatus(@PathVariable int id) {
		statusService.delete(id);
	}
	
}