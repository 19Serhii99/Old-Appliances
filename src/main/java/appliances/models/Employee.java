package appliances.models;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "employee")
public class Employee extends Person {
	
	@NotNull(message = "Start date must be present!")
	private Date startDate;
	
	@NotNull(message = "Position must be present!")
	private Position position;
	
	public Employee() {}

	public Employee(int id) {
		super(id);
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
}