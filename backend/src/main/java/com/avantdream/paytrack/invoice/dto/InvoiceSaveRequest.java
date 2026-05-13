package com.avantdream.paytrack.invoice.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

public class InvoiceSaveRequest {

	@NotNull
	private Long customerId;

	private String notes;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date issueDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date dueDate;

	private String currency;

	private BigDecimal discount;

	private BigDecimal tax;

	@NotEmpty
	@Valid
	private List<InvoiceLineRequest> lines;

	public Long getCustomerId() { return customerId; }
	public void setCustomerId(Long customerId) { this.customerId = customerId; }

	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }

	public Date getIssueDate() { return issueDate; }
	public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

	public Date getDueDate() { return dueDate; }
	public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }

	public BigDecimal getDiscount() { return discount; }
	public void setDiscount(BigDecimal discount) { this.discount = discount; }

	public BigDecimal getTax() { return tax; }
	public void setTax(BigDecimal tax) { this.tax = tax; }

	public List<InvoiceLineRequest> getLines() { return lines; }
	public void setLines(List<InvoiceLineRequest> lines) { this.lines = lines; }

}
