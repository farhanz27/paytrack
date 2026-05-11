package com.avantdream.paytrack.customer.dto;

import java.util.Date;

import com.avantdream.paytrack.customer.entity.Customer;
import com.avantdream.paytrack.customer.entity.CustomerStatus;

public class CustomerResponse {

	private Long id;
	private String customerNumber;
	private String name;
	private String email;
	private String phone;
	private String companyName;
	private String registrationNumber;
	private String billingAddressLine1;
	private String billingAddressLine2;
	private String billingCity;
	private String billingState;
	private String billingPostcode;
	private String billingCountry;
	private String notes;
	private CustomerStatus status;
	private Date archivedAt;
	private Date createdAt;

	public static CustomerResponse from(Customer c) {
		CustomerResponse r = new CustomerResponse();
		r.setId(c.getId());
		r.setCustomerNumber(c.getCustomerNumber());
		r.setName(c.getName());
		r.setEmail(c.getEmail());
		r.setPhone(c.getPhone());
		r.setCompanyName(c.getCompanyName());
		r.setRegistrationNumber(c.getRegistrationNumber());
		r.setBillingAddressLine1(c.getBillingAddressLine1());
		r.setBillingAddressLine2(c.getBillingAddressLine2());
		r.setBillingCity(c.getBillingCity());
		r.setBillingState(c.getBillingState());
		r.setBillingPostcode(c.getBillingPostcode());
		r.setBillingCountry(c.getBillingCountry());
		r.setNotes(c.getNotes());
		r.setStatus(c.getStatus());
		r.setArchivedAt(c.getArchivedAt());
		r.setCreatedAt(c.getCreatedAt());
		return r;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getCustomerNumber() { return customerNumber; }
	public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getCompanyName() { return companyName; }
	public void setCompanyName(String companyName) { this.companyName = companyName; }

	public String getRegistrationNumber() { return registrationNumber; }
	public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

	public String getBillingAddressLine1() { return billingAddressLine1; }
	public void setBillingAddressLine1(String billingAddressLine1) { this.billingAddressLine1 = billingAddressLine1; }

	public String getBillingAddressLine2() { return billingAddressLine2; }
	public void setBillingAddressLine2(String billingAddressLine2) { this.billingAddressLine2 = billingAddressLine2; }

	public String getBillingCity() { return billingCity; }
	public void setBillingCity(String billingCity) { this.billingCity = billingCity; }

	public String getBillingState() { return billingState; }
	public void setBillingState(String billingState) { this.billingState = billingState; }

	public String getBillingPostcode() { return billingPostcode; }
	public void setBillingPostcode(String billingPostcode) { this.billingPostcode = billingPostcode; }

	public String getBillingCountry() { return billingCountry; }
	public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }

	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }

	public CustomerStatus getStatus() { return status; }
	public void setStatus(CustomerStatus status) { this.status = status; }

	public Date getArchivedAt() { return archivedAt; }
	public void setArchivedAt(Date archivedAt) { this.archivedAt = archivedAt; }

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

}
