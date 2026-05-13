package com.avantdream.paytrack.pdf.dto;

import java.math.BigDecimal;

public class PdfItemDTO {

    private String name;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public PdfItemDTO(String name, String description, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
}
