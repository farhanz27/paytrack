package com.avantdream.paytrack.invoice.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.avantdream.paytrack.invoice.entity.InvoiceStatus;

public class InvoiceResponse {

	private Long id;
	private String invoiceNumber;
	private InvoiceStatus status;
	private Date issueDate;
	private Date dueDate;
	private String currency;
	private String notes;
	private BigDecimal discount;
	private BigDecimal tax;
	private BigDecimal subtotal;
	private BigDecimal grandTotal;
	private BigDecimal paidAmount;
	private BigDecimal remainingAmount;
	private Date issuedAt;
	private Date paidAt;
	private Date createdAt;
	private Long customerId;
	private String customerName;
	private String customerCompany;
	private String customerEmail;
	private String billingAddress;
	private Long sourceQuotationId;
	private List<InvoiceLineResponse> items;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getInvoiceNumber() { return invoiceNumber; }
	public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

	public InvoiceStatus getStatus() { return status; }
	public void setStatus(InvoiceStatus status) { this.status = status; }

	public Date getIssueDate() { return issueDate; }
	public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

	public Date getDueDate() { return dueDate; }
	public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }

	public BigDecimal getDiscount() { return discount; }
	public void setDiscount(BigDecimal discount) { this.discount = discount; }

	public BigDecimal getTax() { return tax; }
	public void setTax(BigDecimal tax) { this.tax = tax; }

	public BigDecimal getSubtotal() { return subtotal; }
	public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

	public BigDecimal getGrandTotal() { return grandTotal; }
	public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

	public BigDecimal getPaidAmount() { return paidAmount; }
	public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

	public BigDecimal getRemainingAmount() { return remainingAmount; }
	public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

	public Date getIssuedAt() { return issuedAt; }
	public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }

	public Date getPaidAt() { return paidAt; }
	public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }

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

	public Long getSourceQuotationId() { return sourceQuotationId; }
	public void setSourceQuotationId(Long sourceQuotationId) { this.sourceQuotationId = sourceQuotationId; }

	public List<InvoiceLineResponse> getItems() { return items; }
	public void setItems(List<InvoiceLineResponse> items) { this.items = items; }

}
