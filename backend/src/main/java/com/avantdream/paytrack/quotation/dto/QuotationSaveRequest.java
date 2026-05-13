package com.avantdream.paytrack.quotation.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

public class QuotationSaveRequest {

	@NotNull
	private Long customerId;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date issueDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date validUntil;

	private String currency;

	private BigDecimal discount;

	private BigDecimal tax;

	private String notes;

	@NotEmpty
	@Valid
	private List<QuotationLineRequest> lines;

	public Long getCustomerId() { return customerId; }
	public void setCustomerId(Long customerId) { this.customerId = customerId; }

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

	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }

	public List<QuotationLineRequest> getLines() { return lines; }
	public void setLines(List<QuotationLineRequest> lines) { this.lines = lines; }

}
