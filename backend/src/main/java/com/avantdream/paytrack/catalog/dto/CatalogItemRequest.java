package com.avantdream.paytrack.catalog.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CatalogItemRequest {

	@NotBlank
	@Size(max = 255)
	private String name;

	private String description;

	@DecimalMin(value = "0.00")
	private BigDecimal price;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }

}
