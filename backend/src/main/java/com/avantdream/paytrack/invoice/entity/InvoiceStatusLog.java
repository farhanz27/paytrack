package com.avantdream.paytrack.invoice.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "invoice_status_logs")
public class InvoiceStatusLog implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "invoice_id", nullable = false)
	private Long invoiceId;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 20)
	private InvoiceStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 20)
	private InvoiceStatus toStatus;

	@Column(name = "changed_by", length = 100)
	private String changedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "changed_at", nullable = false, updatable = false)
	private Date changedAt;

	@PrePersist
	public void prePersist() {
		changedAt = new Date();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getInvoiceId() { return invoiceId; }
	public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

	public InvoiceStatus getFromStatus() { return fromStatus; }
	public void setFromStatus(InvoiceStatus fromStatus) { this.fromStatus = fromStatus; }

	public InvoiceStatus getToStatus() { return toStatus; }
	public void setToStatus(InvoiceStatus toStatus) { this.toStatus = toStatus; }

	public String getChangedBy() { return changedBy; }
	public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

	public Date getChangedAt() { return changedAt; }
	public void setChangedAt(Date changedAt) { this.changedAt = changedAt; }

	private static final long serialVersionUID = 1L;

}
