package com.avantdream.paytrack.invoice.dto;

import jakarta.validation.constraints.NotNull;

import com.avantdream.paytrack.invoice.entity.InvoiceStatus;

public class InvoiceStatusRequest {

	@NotNull
	private InvoiceStatus status;

	public InvoiceStatus getStatus() { return status; }
	public void setStatus(InvoiceStatus status) { this.status = status; }

}
