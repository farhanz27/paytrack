package com.avantdream.paytrack.invoice.entity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum InvoiceStatus {
	DRAFT,
	ISSUED,
	PARTIALLY_PAID,
	PAID,
	CANCELLED;

	private static final Map<InvoiceStatus, Set<InvoiceStatus>> TRANSITIONS = Map.of(
		DRAFT,          EnumSet.of(ISSUED, CANCELLED),
		ISSUED,         EnumSet.of(PARTIALLY_PAID, PAID, CANCELLED),
		PARTIALLY_PAID, EnumSet.of(PAID, CANCELLED),
		PAID,           EnumSet.noneOf(InvoiceStatus.class),
		CANCELLED,      EnumSet.noneOf(InvoiceStatus.class)
	);

	public boolean canTransitionTo(InvoiceStatus next) {
		return TRANSITIONS.getOrDefault(this, EnumSet.noneOf(InvoiceStatus.class)).contains(next);
	}

	public boolean isTerminal() {
		return this == PAID || this == CANCELLED;
	}
}
