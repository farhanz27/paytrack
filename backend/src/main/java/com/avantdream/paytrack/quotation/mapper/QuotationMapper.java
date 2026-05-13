package com.avantdream.paytrack.quotation.mapper;

import java.util.stream.Collectors;

import com.avantdream.paytrack.quotation.dto.QuotationLineResponse;
import com.avantdream.paytrack.quotation.dto.QuotationResponse;
import com.avantdream.paytrack.quotation.dto.QuotationSummaryResponse;
import com.avantdream.paytrack.quotation.entity.Quotation;
import com.avantdream.paytrack.quotation.entity.QuotationItem;

public class QuotationMapper {

	private QuotationMapper() {}

	public static QuotationResponse toResponse(Quotation quotation) {
		QuotationResponse response = new QuotationResponse();
		response.setId(quotation.getId());
		response.setQuotationNumber(quotation.getQuotationNumber());
		response.setStatus(quotation.getStatus());
		response.setIssueDate(quotation.getIssueDate());
		response.setValidUntil(quotation.getValidUntil());
		response.setCurrency(quotation.getCurrency());
		response.setDiscount(quotation.getDiscount());
		response.setTax(quotation.getTax());
		response.setSubtotal(quotation.getSubtotal());
		response.setGrandTotal(quotation.getGrandTotal());
		response.setNotes(quotation.getNotes());
		response.setCreatedAt(quotation.getCreatedAt());
		response.setCustomerName(quotation.getCustomerName());
		response.setCustomerCompany(quotation.getCustomerCompany());
		response.setCustomerEmail(quotation.getCustomerEmail());
		response.setBillingAddress(quotation.getBillingAddress());
		if (quotation.getCustomer() != null) {
			response.setCustomerId(quotation.getCustomer().getId());
		}
		if (quotation.getItems() != null) {
			response.setItems(quotation.getItems().stream()
					.map(QuotationMapper::toLineResponse)
					.collect(Collectors.toList()));
		}
		return response;
	}

	public static QuotationSummaryResponse toSummaryResponse(Quotation quotation) {
		QuotationSummaryResponse response = new QuotationSummaryResponse();
		response.setId(quotation.getId());
		response.setQuotationNumber(quotation.getQuotationNumber());
		response.setStatus(quotation.getStatus());
		response.setIssueDate(quotation.getIssueDate());
		response.setValidUntil(quotation.getValidUntil());
		response.setCreatedAt(quotation.getCreatedAt());
		response.setGrandTotal(quotation.getGrandTotal());
		response.setCustomerName(quotation.getCustomerName());
		return response;
	}

	private static QuotationLineResponse toLineResponse(QuotationItem item) {
		QuotationLineResponse line = new QuotationLineResponse();
		line.setId(item.getId());
		line.setName(item.getName());
		line.setDescription(item.getDescription());
		line.setUnitPrice(item.getUnitPrice());
		line.setQuantity(item.getQuantity());
		line.setSubtotal(item.getSubtotal());
		return line;
	}

}
