package com.avantdream.paytrack.invoice.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.avantdream.paytrack.invoice.entity.InvoiceStatus;

public class InvoiceSummaryResponse {

	private Long id;
	private String invoiceNumber;
	private InvoiceStatus status;
	private Date issueDate;
	private Date dueDate;
	private Date createdAt;
	private BigDecimal grandTotal;
	private String customerName;

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

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

	public BigDecimal getGrandTotal() { return grandTotal; }
	public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

}
