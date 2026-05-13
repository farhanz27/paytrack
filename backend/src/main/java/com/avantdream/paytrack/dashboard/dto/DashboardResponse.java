package com.avantdream.paytrack.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardResponse {

	private Kpis kpis;
	private Charts charts;
	private Metrics metrics;

	public static class Kpis {
		private BigDecimal totalRevenue;
		private BigDecimal outstandingAmount;

		public BigDecimal getTotalRevenue() { return totalRevenue; }
		public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

		public BigDecimal getOutstandingAmount() { return outstandingAmount; }
		public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }
	}

	public static class MonthlyRevenue {
		private List<String> labels;
		private List<BigDecimal> data;

		public List<String> getLabels() { return labels; }
		public void setLabels(List<String> labels) { this.labels = labels; }

		public List<BigDecimal> getData() { return data; }
		public void setData(List<BigDecimal> data) { this.data = data; }
	}

	public static class Charts {
		private MonthlyRevenue monthlyRevenue;
		private Map<String, Long> invoiceStatusDistribution;
		private Map<String, Long> quotationStatusDistribution;

		public MonthlyRevenue getMonthlyRevenue() { return monthlyRevenue; }
		public void setMonthlyRevenue(MonthlyRevenue monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

		public Map<String, Long> getInvoiceStatusDistribution() { return invoiceStatusDistribution; }
		public void setInvoiceStatusDistribution(Map<String, Long> invoiceStatusDistribution) { this.invoiceStatusDistribution = invoiceStatusDistribution; }

		public Map<String, Long> getQuotationStatusDistribution() { return quotationStatusDistribution; }
		public void setQuotationStatusDistribution(Map<String, Long> quotationStatusDistribution) { this.quotationStatusDistribution = quotationStatusDistribution; }
	}

	public static class Metrics {
		private double quotationConversionRate;

		public double getQuotationConversionRate() { return quotationConversionRate; }
		public void setQuotationConversionRate(double quotationConversionRate) { this.quotationConversionRate = quotationConversionRate; }
	}

	public Kpis getKpis() { return kpis; }
	public void setKpis(Kpis kpis) { this.kpis = kpis; }

	public Charts getCharts() { return charts; }
	public void setCharts(Charts charts) { this.charts = charts; }

	public Metrics getMetrics() { return metrics; }
	public void setMetrics(Metrics metrics) { this.metrics = metrics; }
}
