package com.avantdream.paytrack.invoice.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.customer.entity.Customer;

@Entity
@Table(name = "invoices", uniqueConstraints = @UniqueConstraint(name = "uq_invoices_company_number", columnNames = {"company_id", "invoice_number"}))
public class Invoice implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "invoice_number", length = 50)
	private String invoiceNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private InvoiceStatus status = InvoiceStatus.DRAFT;

	@Temporal(TemporalType.DATE)
	@Column(name = "issue_date")
	private Date issueDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "due_date")
	private Date dueDate;

	@Column(name = "currency", length = 10)
	private String currency = "MYR";

	@Column(name = "notes")
	private String notes;

	@Column(name = "discount", precision = 12, scale = 2)
	private BigDecimal discount;

	@Column(name = "tax", precision = 12, scale = 2)
	private BigDecimal tax;

	@Column(name = "subtotal", precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(name = "grand_total", precision = 12, scale = 2)
	private BigDecimal grandTotal;

	@Column(name = "paid_amount", precision = 12, scale = 2)
	private BigDecimal paidAmount = BigDecimal.ZERO;

	@Column(name = "remaining_amount", precision = 12, scale = 2)
	private BigDecimal remainingAmount = BigDecimal.ZERO;

	// Customer snapshot — captured at creation, immutable to preserve historical accuracy
	@Column(name = "customer_name", length = 255)
	private String customerName;

	@Column(name = "customer_company", length = 255)
	private String customerCompany;

	@Column(name = "customer_email", length = 255)
	private String customerEmail;

	@Column(name = "billing_address", columnDefinition = "TEXT")
	private String billingAddress;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "issued_at")
	private Date issuedAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "paid_at")
	private Date paidAt;

	@Column(name = "source_quotation_id")
	private Long sourceQuotationId;

	@Column(name = "pdf_s3_key", length = 500)
	private String pdfS3Key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", updatable = false)
	private Date createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	@JsonIgnore
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoiceItem> items = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		createdAt = new Date();
	}

	public void addItem(InvoiceItem item) {
		item.setInvoice(this);
		items.add(item);
	}

	// Kept for backward compat with views (PDF/XLSX)
	public BigDecimal getTotalInvoice() {
		return grandTotal != null ? grandTotal : BigDecimal.ZERO;
	}

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

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

	public String getCustomerCompany() { return customerCompany; }
	public void setCustomerCompany(String customerCompany) { this.customerCompany = customerCompany; }

	public String getCustomerEmail() { return customerEmail; }
	public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

	public String getBillingAddress() { return billingAddress; }
	public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

	public Date getIssuedAt() { return issuedAt; }
	public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }

	public Date getPaidAt() { return paidAt; }
	public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }

	public Long getSourceQuotationId() { return sourceQuotationId; }
	public void setSourceQuotationId(Long sourceQuotationId) { this.sourceQuotationId = sourceQuotationId; }

	public String getPdfS3Key() { return pdfS3Key; }
	public void setPdfS3Key(String pdfS3Key) { this.pdfS3Key = pdfS3Key; }

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

	public Company getCompany() { return company; }
	public void setCompany(Company company) { this.company = company; }

	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }

	public List<InvoiceItem> getItems() { return items; }
	public void setItems(List<InvoiceItem> items) { this.items = items; }

	public void addInvoiceItem(InvoiceItem item) { item.setInvoice(this); items.add(item); }

	private static final long serialVersionUID = 1L;

}
