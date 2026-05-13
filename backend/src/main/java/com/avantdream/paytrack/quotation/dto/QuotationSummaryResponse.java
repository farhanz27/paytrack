package com.avantdream.paytrack.quotation.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.avantdream.paytrack.quotation.entity.QuotationStatus;

public class QuotationSummaryResponse {

	private Long id;
	private String quotationNumber;
	private QuotationStatus status;
	private Date issueDate;
	private Date validUntil;
	private Date createdAt;
	private BigDecimal grandTotal;
	private String customerName;

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

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

	public BigDecimal getGrandTotal() { return grandTotal; }
	public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

}
