package com.fintrack.controller;
import com.fintrack.dto.TransactionFilter;
import com.fintrack.dto.Views.*;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;
@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final AnalyticsService analytics;
    private final TransactionService transactions;
    public DashboardController(AnalyticsService analytics, TransactionService transactions) { this.analytics = analytics; this.transactions = transactions; }
    @GetMapping("/summary") public Summary summary(@AuthenticationPrincipal CurrentUser u) { return analytics.summary(u.id()); }
    @GetMapping("/monthly") public List<MonthlyTotal> monthly(@AuthenticationPrincipal CurrentUser u, @RequestParam(required = false) Integer year) { return analytics.monthly(u.id(), year == null ? LocalDate.now().getYear() : year); }
    @GetMapping("/category-expenses") public List<CategoryTotal> categories(@AuthenticationPrincipal CurrentUser u, @RequestParam(required = false) LocalDate start, @RequestParam(required = false) LocalDate end) {
        YearMonth now = YearMonth.now(); return analytics.categories(u.id(), start == null ? now.atDay(1) : start, end == null ? now.atEndOfMonth() : end);
    }
    @GetMapping("/recent-transactions") public List<TransactionView> recent(@AuthenticationPrincipal CurrentUser u) { return transactions.list(u.id(), new TransactionFilter(null, null, null, null, null, null, "transactionDate", "desc", 0, 5)).content(); }
}
