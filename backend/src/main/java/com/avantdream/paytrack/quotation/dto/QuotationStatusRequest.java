package com.avantdream.paytrack.quotation.dto;

import jakarta.validation.constraints.NotNull;

import com.avantdream.paytrack.quotation.entity.QuotationStatus;

public class QuotationStatusRequest {

	@NotNull
	private QuotationStatus status;

	public QuotationStatus getStatus() { return status; }
	public void setStatus(QuotationStatus status) { this.status = status; }

}
