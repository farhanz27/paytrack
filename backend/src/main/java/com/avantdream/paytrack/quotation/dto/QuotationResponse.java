package com.avantdream.paytrack.quotation.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.avantdream.paytrack.quotation.entity.QuotationStatus;

public class QuotationResponse {

	private Long id;
	private String quotationNumber;
	private QuotationStatus status;
	private Date issueDate;
	private Date validUntil;
	private String currency;
	private BigDecimal discount;
	private BigDecimal tax;
	private BigDecimal subtotal;
	private BigDecimal grandTotal;
	private String notes;
	private Date createdAt;
	private Long customerId;
	private String customerName;
	private String customerCompany;
	private String customerEmail;
	private String billingAddress;
	private List<QuotationLineResponse> items;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getQuotationNumber() { return quotationNumber; }
	public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }

	public QuotationStatus getStatus() { return status; }
	public void setStatus(QuotationStatus status) { this.status = status; }

	public Date getIssueDate() { return issueDate; }
	public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

	public Date getValidUntil() { return validUntil; }
	public void setValidUntil(Date validUntil) { this.validUntil = validUntil; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public BigDecimal getDiscount() { return discount; }
	public void setDiscount(BigDecimal discount) { this.discount = discount; }

	public BigDecimal getTax() { return tax; }
	public void setTax(BigDecimal tax) { this.tax = tax; }

	public BigDecimal getSubtotal() { return subtotal; }
	public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

	public BigDecimal getGrandTotal() { return grandTotal; }
	public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

	public Long getCustomerId() { return customerId; }
	public void setCustomerId(Long customerId) { this.customerId = customerId; }

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

	public String getCustomerCompany() { return customerCompany; }
	public void setCustomerCompany(String customerCompany) { this.customerCompany = customerCompany; }

	public String getCustomerEmail() { return customerEmail; }
	public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

	public String getBillingAddress() { return billingAddress; }
	public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

	public List<QuotationLineResponse> getItems() { return items; }
	public void setItems(List<QuotationLineResponse> items) { this.items = items; }

}
