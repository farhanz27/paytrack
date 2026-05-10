package com.avantdream.paytrack.company.dto;

import com.avantdream.paytrack.company.entity.Company;

public class CompanyResponse {

	private Long id;
	private String name;
	private String registrationNumber;
	private String status;
	private String email;
	private String phone;
	private String billingAddressLine1;
	private String billingAddressLine2;
	private String billingCity;
	private String billingState;
	private String billingPostcode;
	private String billingCountry;
	private String defaultCurrency;

	public static CompanyResponse from(Company c) {
		CompanyResponse r = new CompanyResponse();
		r.setId(c.getId());
		r.setName(c.getName());
		r.setRegistrationNumber(c.getRegistrationNumber());
		r.setStatus(c.getStatus());
		r.setEmail(c.getEmail());
		r.setPhone(c.getPhone());
		r.setBillingAddressLine1(c.getBillingAddressLine1());
		r.setBillingAddressLine2(c.getBillingAddressLine2());
		r.setBillingCity(c.getBillingCity());
		r.setBillingState(c.getBillingState());
		r.setBillingPostcode(c.getBillingPostcode());
		r.setBillingCountry(c.getBillingCountry());
		r.setDefaultCurrency(c.getDefaultCurrency() != null ? c.getDefaultCurrency() : "MYR");
		return r;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getRegistrationNumber() { return registrationNumber; }
	public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getBillingAddressLine1() { return billingAddressLine1; }
	public void setBillingAddressLine1(String v) { this.billingAddressLine1 = v; }

	public String getBillingAddressLine2() { return billingAddressLine2; }
	public void setBillingAddressLine2(String v) { this.billingAddressLine2 = v; }

	public String getBillingCity() { return billingCity; }
	public void setBillingCity(String billingCity) { this.billingCity = billingCity; }

	public String getBillingState() { return billingState; }
	public void setBillingState(String billingState) { this.billingState = billingState; }

	public String getBillingPostcode() { return billingPostcode; }
	public void setBillingPostcode(String billingPostcode) { this.billingPostcode = billingPostcode; }

	public String getBillingCountry() { return billingCountry; }
	public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }

	public String getDefaultCurrency() { return defaultCurrency; }
	public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }

}
