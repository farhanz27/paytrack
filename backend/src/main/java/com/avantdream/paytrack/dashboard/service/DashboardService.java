package com.avantdream.paytrack.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.dashboard.dto.DashboardResponse;
import com.avantdream.paytrack.invoice.entity.InvoiceStatus;
import com.avantdream.paytrack.invoice.repository.InvoiceRepository;
import com.avantdream.paytrack.quotation.entity.QuotationStatus;
import com.avantdream.paytrack.quotation.repository.QuotationRepository;

@Service
public class DashboardService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Kuala_Lumpur");

	private final InvoiceRepository invoiceRepository;
	private final QuotationRepository quotationRepository;

	public DashboardService(InvoiceRepository invoiceRepository, QuotationRepository quotationRepository) {
		this.invoiceRepository = invoiceRepository;
		this.quotationRepository = quotationRepository;
	}

	@Transactional(readOnly = true)
	public DashboardResponse getDashboard(Long companyId, LocalDate from, LocalDate to) {
		LocalDate effectiveTo = to != null ? to : LocalDate.now(ZONE);
		LocalDate effectiveFrom = from != null ? from : effectiveTo.minusMonths(6).withDayOfMonth(1);

		Date fromDate = toStartOfDay(effectiveFrom);
		Date toDate = toEndOfDay(effectiveTo);

		BigDecimal totalRevenue = invoiceRepository.sumTotalRevenue(companyId, fromDate, toDate);
		BigDecimal outstandingAmount = invoiceRepository.sumOutstandingAmount(companyId,
				EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID));

		Map<String, Long> invoiceDist = buildInvoiceStatusDistribution(companyId);
		Map<String, Long> quotationDist = buildQuotationStatusDistribution(companyId);

		DashboardResponse.MonthlyRevenue monthlyRevenue =
				buildMonthlyRevenue(companyId, fromDate, toDate, effectiveFrom, effectiveTo);

		long issuedCount = quotationDist.getOrDefault(QuotationStatus.ISSUED.name(), 0L);
		long acceptedCount = quotationDist.getOrDefault(QuotationStatus.ACCEPTED.name(), 0L);
		double conversionRate = issuedCount == 0 ? 0.0
				: BigDecimal.valueOf(acceptedCount)
						.divide(BigDecimal.valueOf(issuedCount), 4, RoundingMode.HALF_UP)
						.doubleValue();

		DashboardResponse.Kpis kpis = new DashboardResponse.Kpis();
		kpis.setTotalRevenue(totalRevenue);
		kpis.setOutstandingAmount(outstandingAmount);

		DashboardResponse.Charts charts = new DashboardResponse.Charts();
		charts.setMonthlyRevenue(monthlyRevenue);
		charts.setInvoiceStatusDistribution(invoiceDist);
		charts.setQuotationStatusDistribution(quotationDist);

		DashboardResponse.Metrics metrics = new DashboardResponse.Metrics();
		metrics.setQuotationConversionRate(conversionRate);

		DashboardResponse response = new DashboardResponse();
		response.setKpis(kpis);
		response.setCharts(charts);
		response.setMetrics(metrics);
		return response;
	}

	private Map<String, Long> buildInvoiceStatusDistribution(Long companyId) {
		Map<String, Long> result = new HashMap<>();
		for (InvoiceStatus s : InvoiceStatus.values()) result.put(s.name(), 0L);
		for (Object[] row : invoiceRepository.countGroupByStatus(companyId)) {
			result.put(((InvoiceStatus) row[0]).name(), (Long) row[1]);
		}
		return result;
	}

	private Map<String, Long> buildQuotationStatusDistribution(Long companyId) {
		Map<String, Long> result = new HashMap<>();
		for (QuotationStatus s : QuotationStatus.values()) result.put(s.name(), 0L);
		for (Object[] row : quotationRepository.countGroupByStatus(companyId)) {
			result.put(((QuotationStatus) row[0]).name(), (Long) row[1]);
		}
		return result;
	}

	private DashboardResponse.MonthlyRevenue buildMonthlyRevenue(
			Long companyId, Date fromDate, Date toDate, LocalDate effectiveFrom, LocalDate effectiveTo) {

		// Build full month range
		List<String> labels = new ArrayList<>();
		YearMonth cursor = YearMonth.from(effectiveFrom);
		YearMonth end = YearMonth.from(effectiveTo);
		while (!cursor.isAfter(end)) {
			labels.add(cursor.toString()); // YYYY-MM
			cursor = cursor.plusMonths(1);
		}

		// Query actual data
		Map<String, BigDecimal> revenueByMonth = new HashMap<>();
		for (Object[] row : invoiceRepository.monthlyRevenue(companyId, fromDate, toDate)) {
			revenueByMonth.put((String) row[0], new BigDecimal(row[1].toString()).setScale(2, RoundingMode.HALF_UP));
		}

		List<BigDecimal> data = new ArrayList<>();
		for (String label : labels) {
			data.add(revenueByMonth.getOrDefault(label, BigDecimal.ZERO));
		}

		DashboardResponse.MonthlyRevenue result = new DashboardResponse.MonthlyRevenue();
		result.setLabels(labels);
		result.setData(data);
		return result;
	}

	private Date toStartOfDay(LocalDate date) {
		return Date.from(date.atStartOfDay(ZONE).toInstant());
	}

	private Date toEndOfDay(LocalDate date) {
		return Date.from(date.atTime(23, 59, 59).atZone(ZONE).toInstant());
	}
}
