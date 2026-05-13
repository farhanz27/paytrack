package com.avantdream.paytrack.invoice.mapper;

import java.util.stream.Collectors;

import com.avantdream.paytrack.invoice.dto.InvoiceLineResponse;
import com.avantdream.paytrack.invoice.dto.InvoiceResponse;
import com.avantdream.paytrack.invoice.dto.InvoiceSummaryResponse;
import com.avantdream.paytrack.invoice.entity.Invoice;
import com.avantdream.paytrack.invoice.entity.InvoiceItem;

public class InvoiceMapper {

	private InvoiceMapper() {}

	public static InvoiceResponse toResponse(Invoice invoice) {
		InvoiceResponse response = new InvoiceResponse();
		response.setId(invoice.getId());
		response.setInvoiceNumber(invoice.getInvoiceNumber());
		response.setStatus(invoice.getStatus());
		response.setIssueDate(invoice.getIssueDate());
		response.setDueDate(invoice.getDueDate());
		response.setCurrency(invoice.getCurrency());
		response.setNotes(invoice.getNotes());
		response.setDiscount(invoice.getDiscount());
		response.setTax(invoice.getTax());
		response.setSubtotal(invoice.getSubtotal());
		response.setGrandTotal(invoice.getGrandTotal());
		response.setPaidAmount(invoice.getPaidAmount());
		response.setRemainingAmount(invoice.getRemainingAmount());
		response.setIssuedAt(invoice.getIssuedAt());
		response.setPaidAt(invoice.getPaidAt());
		response.setCreatedAt(invoice.getCreatedAt());
		response.setCustomerName(invoice.getCustomerName());
		response.setCustomerCompany(invoice.getCustomerCompany());
		response.setCustomerEmail(invoice.getCustomerEmail());
		response.setBillingAddress(invoice.getBillingAddress());
		response.setSourceQuotationId(invoice.getSourceQuotationId());
		if (invoice.getCustomer() != null) {
			response.setCustomerId(invoice.getCustomer().getId());
		}
		if (invoice.getItems() != null) {
			response.setItems(invoice.getItems().stream()
					.map(InvoiceMapper::toLineResponse)
					.collect(Collectors.toList()));
		}
		return response;
	}

	public static InvoiceSummaryResponse toSummaryResponse(Invoice invoice) {
		InvoiceSummaryResponse response = new InvoiceSummaryResponse();
		response.setId(invoice.getId());
		response.setInvoiceNumber(invoice.getInvoiceNumber());
		response.setStatus(invoice.getStatus());
		response.setIssueDate(invoice.getIssueDate());
		response.setDueDate(invoice.getDueDate());
		response.setCreatedAt(invoice.getCreatedAt());
		response.setGrandTotal(invoice.getGrandTotal());
		response.setCustomerName(invoice.getCustomerName());
		return response;
	}

	private static InvoiceLineResponse toLineResponse(InvoiceItem item) {
		InvoiceLineResponse line = new InvoiceLineResponse();
		line.setId(item.getId());
		line.setName(item.getName());
		line.setDescription(item.getDescription());
		line.setUnitPrice(item.getUnitPrice());
		line.setQuantity(item.getQuantity());
		line.setSubtotal(item.getSubtotal());
		return line;
	}

}
