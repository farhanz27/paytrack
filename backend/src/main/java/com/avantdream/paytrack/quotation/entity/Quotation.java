package com.avantdream.paytrack.quotation.entity;

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
@Table(name = "quotations", uniqueConstraints = @UniqueConstraint(name = "uq_quotations_company_number", columnNames = {"company_id", "quotation_number"}))
public class Quotation implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "quotation_number", length = 50)
	private String quotationNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private QuotationStatus status = QuotationStatus.DRAFT;

	@Temporal(TemporalType.DATE)
	@Column(name = "issue_date")
	private Date issueDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "valid_until")
	private Date validUntil;

	@Column(name = "currency", length = 10)
	private String currency = "MYR";

	// Customer snapshot — captured at creation, immutable to preserve historical accuracy
	@Column(name = "customer_name", length = 255)
	private String customerName;

	@Column(name = "customer_company", length = 255)
	private String customerCompany;

	@Column(name = "customer_email", length = 255)
	private String customerEmail;

	@Column(name = "billing_address", columnDefinition = "TEXT")
	private String billingAddress;

	@Column(name = "discount", precision = 12, scale = 2)
	private BigDecimal discount;

	@Column(name = "tax", precision = 12, scale = 2)
	private BigDecimal tax;

	@Column(name = "subtotal", precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(name = "grand_total", precision = 12, scale = 2)
	private BigDecimal grandTotal;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

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

	@OneToMany(mappedBy = "quotation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QuotationItem> items = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		createdAt = new Date();
	}

	public void addItem(QuotationItem item) {
		item.setQuotation(this);
		items.add(item);
	}

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

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

	public String getCustomerCompany() { return customerCompany; }
	public void setCustomerCompany(String customerCompany) { this.customerCompany = customerCompany; }

	public String getCustomerEmail() { return customerEmail; }
	public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

	public String getBillingAddress() { return billingAddress; }
	public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

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

	public String getPdfS3Key() { return pdfS3Key; }
	public void setPdfS3Key(String pdfS3Key) { this.pdfS3Key = pdfS3Key; }

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

	public Company getCompany() { return company; }
	public void setCompany(Company company) { this.company = company; }

	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }

	public List<QuotationItem> getItems() { return items; }
	public void setItems(List<QuotationItem> items) { this.items = items; }

	private static final long serialVersionUID = 1L;

}
