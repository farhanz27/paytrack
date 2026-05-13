package com.avantdream.paytrack.invoice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InvoiceLineRequest {

	@NotBlank
	private String name;

	private String description;

	@NotNull
	@DecimalMin("0.01")
	private BigDecimal unitPrice;

	@NotNull
	@Min(1)
	private Integer quantity;

	// Optional: used to validate archived status during request processing; not stored in entity
	private Long catalogItemId;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public BigDecimal getUnitPrice() { return unitPrice; }
	public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }

	public Long getCatalogItemId() { return catalogItemId; }
	public void setCatalogItemId(Long catalogItemId) { this.catalogItemId = catalogItemId; }

}
