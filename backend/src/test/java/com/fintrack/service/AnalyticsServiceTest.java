package com.fintrack.service;
import com.fintrack.entity.TransactionType;
import com.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock TransactionRepository transactions;
    @Mock BudgetService budgets;
    @Mock GoalService goals;
    @InjectMocks AnalyticsService analytics;
    @Test void dashboardUsesAggregatesAndComputesExactSavings() {
        when(transactions.totals(eq(1L), any(), any())).thenReturn(List.of(new Object[]{TransactionType.INCOME, new BigDecimal("10000"), 1L, new BigDecimal("10000")}, new Object[]{TransactionType.EXPENSE, new BigDecimal("2500"), 2L, new BigDecimal("2000")}));
        when(transactions.categoryTotals(eq(1L), any(), any())).thenReturn(List.of()); when(budgets.list(eq(1L), anyInt(), anyInt())).thenReturn(List.of()); when(goals.list(1L)).thenReturn(List.of());
        var summary = analytics.summary(1L);
        assertEquals(new BigDecimal("7500"), summary.balance()); assertEquals(new BigDecimal("75.00"), summary.savingsRate()); assertEquals(3, summary.transactionCount()); assertEquals(BigDecimal.ZERO, summary.budgetUsage());
        verify(transactions, never()).findAll();
    }
    @Test void reportCalculatesHighestAndAverageWithZeroSafeEmptyState() {
        var report = analytics.report(1L, LocalDate.now(), LocalDate.now());
        assertEquals(BigDecimal.ZERO, report.averageTransaction()); assertEquals(BigDecimal.ZERO, report.highestExpense()); assertEquals(0, report.transactionCount());
    }
    @Test void monthlyChartIncludesZeroMonths() { assertEquals(12, analytics.monthly(1L, 2026).size()); assertEquals("2026-01", analytics.monthly(1L, 2026).get(0).month()); }
}
