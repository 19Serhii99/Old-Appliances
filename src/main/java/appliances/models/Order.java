package appliances.models;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.Nullable;

@Document(collection = "order")
public class Order extends Entity {

	@Nullable
	private User user;

	@Nullable
	private Employee employee;

	@NotEmpty(message = "Last name must be present!")
	@Size(min = 2, max = 50, message = "Last name must have 2 to 50 characters!")
	private String lastName;

	@NotEmpty(message = "First name must be present!")
	@Size(min = 2, max = 50, message = "First name must have 2 to 50 characters!")
	private String firstName;

	@Nullable
	@Size(min = 2, max = 50, message = "Middle name must have 2 to 50 characters!")
	private String middleName;

	@NotEmpty(message = "Phone number must be present!")
	@Pattern(regexp = "^[[+]?[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*]{10,15}$", message = "Invalid phone number!")
	private String phone;

	@AssertFalse(message = "The order cannot be processed at the moment!")
	private boolean processing;

	@AssertFalse(message = "The order cannot be executed at the moment!")
	private boolean executed;

	@NotEmpty(message = "City must be present!")
	@Size(min = 2, max = 50, message = "City must have 2 to 50 characters!")
	private String city;

	@NotNull(message = "Post office number must be present!")
	private Integer postOffice;

	@Nullable
	@Size(min = 11, max = 11, message = "Nova poshta waybill number must be 11 characters!")
	private String novaPoshtaNo;

	@Min(value = 0, message = "Delivery cost must be positive!")
	private float deliveryCost;

	@AssertFalse(message = "The order cannot be canceled at the moment!")
	private boolean canceled;

	@Nullable
	private Status status;

	@Min(value = 0, message = "Delivery cost must be positive!")
	private float total;

	private List<Basket> baskets;
	
	@Nullable
	private Date creationTime;

	public Order() {
	}

	public Order(int id) {
		super(id);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isProcessing() {
		return processing;
	}

	public void setProcessing(boolean processing) {
		this.processing = processing;
	}

	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getPostOffice() {
		return postOffice;
	}

	public void setPostOffice(Integer postOffice) {
		this.postOffice = postOffice;
	}

	public String getNovaPoshtaNo() {
		return novaPoshtaNo;
	}

	public void setNovaPoshtaNo(String novaPoshtaNo) {
		this.novaPoshtaNo = novaPoshtaNo;
	}

	public float getDeliveryCost() {
		return deliveryCost;
	}

	public void setDeliveryCost(float deliveryCost) {
		this.deliveryCost = deliveryCost;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public float getTotal() {
		return total;
	}

	public void setTotal(float total) {
		this.total = total;
	}

	public List<Basket> getBaskets() {
		return baskets;
	}

	public void setBaskets(List<Basket> baskets) {
		this.baskets = baskets;
	}
	
	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public String toString() {
		return "Order [user=" + user + ", employee=" + employee + ", lastName=" + lastName + ", firstName=" + firstName
				+ ", middleName=" + middleName + ", phone=" + phone + ", processing=" + processing + ", executed="
				+ executed + ", city=" + city + ", postOffice=" + postOffice + ", novaPoshtaNo=" + novaPoshtaNo
				+ ", deliveryCost=" + deliveryCost + ", canceled=" + canceled + ", status=" + status + ", total="
				+ total + ", baskets=" + baskets + ", creationTime=" + creationTime + "]";
	}

}