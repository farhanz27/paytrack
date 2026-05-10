package com.avantdream.paytrack.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyRequest {

	@NotBlank
	@Size(max = 255)
	private String name;

	@NotBlank
	@Size(max = 100)
	private String registrationNumber;

	@Size(max = 255)
	private String email;

	@Size(max = 50)
	private String phone;

	@Size(max = 255)
	private String billingAddressLine1;

	@Size(max = 255)
	private String billingAddressLine2;

	@Size(max = 100)
	private String billingCity;

	@Size(max = 100)
	private String billingState;

	@Size(max = 20)
	private String billingPostcode;

	@Size(max = 100)
	private String billingCountry;

	@Size(max = 10)
	private String defaultCurrency;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getRegistrationNumber() { return registrationNumber; }
	public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

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
