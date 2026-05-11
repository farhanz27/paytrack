package com.avantdream.paytrack.catalog.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.avantdream.paytrack.catalog.entity.CatalogItem;

public class CatalogItemResponse {

	private Long id;
	private String itemCode;
	private String name;
	private String description;
	private BigDecimal price;
	private Date archivedAt;
	private Date createdAt;

	public static CatalogItemResponse from(CatalogItem item) {
		CatalogItemResponse r = new CatalogItemResponse();
		r.setId(item.getId());
		r.setItemCode(item.getItemCode());
		r.setName(item.getName());
		r.setDescription(item.getDescription());
		r.setPrice(item.getPrice());
		r.setArchivedAt(item.getArchivedAt());
		r.setCreatedAt(item.getCreatedAt());
		return r;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getItemCode() { return itemCode; }
	public void setItemCode(String itemCode) { this.itemCode = itemCode; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }

	public Date getArchivedAt() { return archivedAt; }
	public void setArchivedAt(Date archivedAt) { this.archivedAt = archivedAt; }

	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

}
