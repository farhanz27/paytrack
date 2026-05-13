package com.avantdream.paytrack.quotation.entity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum QuotationStatus {
	DRAFT,
	ISSUED,
	ACCEPTED,
	REJECTED,
	EXPIRED;

	private static final Map<QuotationStatus, Set<QuotationStatus>> TRANSITIONS = Map.of(
		DRAFT,    EnumSet.of(ISSUED),
		ISSUED,   EnumSet.of(ACCEPTED, REJECTED, EXPIRED),
		ACCEPTED, EnumSet.noneOf(QuotationStatus.class),
		REJECTED, EnumSet.noneOf(QuotationStatus.class),
		EXPIRED,  EnumSet.noneOf(QuotationStatus.class)
	);

	public boolean canTransitionTo(QuotationStatus next) {
		return TRANSITIONS.getOrDefault(this, EnumSet.noneOf(QuotationStatus.class)).contains(next);
	}

	public boolean isTerminal() {
		return this == ACCEPTED || this == REJECTED || this == EXPIRED;
	}
}
