package com.fintrack.service;
import com.fintrack.dto.Views.*;
import com.fintrack.entity.TransactionType;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service @Transactional(readOnly = true)
public class AnalyticsService {
    private final TransactionRepository transactions;
    private final BudgetService budgets;
    private final GoalService goals;
    public AnalyticsService(TransactionRepository transactions, BudgetService budgets, GoalService goals) { this.transactions = transactions; this.budgets = budgets; this.goals = goals; }
    public List<CategoryTotal> categories(Long uid, LocalDate start, LocalDate end) {
        DateRanges.validate(start, end);
        return transactions.categoryTotals(uid, start, end).stream().map(r -> new CategoryTotal((Long) r[0], (String) r[1], (BigDecimal) r[2])).toList();
    }
    public Report report(Long uid, YearMonth month) { return report(uid, month.atDay(1), month.atEndOfMonth()); }
    public Report report(Long uid, LocalDate start, LocalDate end) {
        DateRanges.validate(start, end);
        BigDecimal income = BigDecimal.ZERO, expense = BigDecimal.ZERO, highest = BigDecimal.ZERO;
        long count = 0;
        for (Object[] row : transactions.totals(uid, start, end)) {
            count += ((Number) row[2]).longValue();
            if (row[0] == TransactionType.INCOME) income = (BigDecimal) row[1];
            else { expense = (BigDecimal) row[1]; highest = (BigDecimal) row[3]; }
        }
        BigDecimal average = count == 0 ? BigDecimal.ZERO : income.add(expense).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return new Report(start, end, income, expense, income.subtract(expense), count, highest, average, categories(uid, start, end));
    }
    public Summary summary(Long uid) {
        Report all = report(uid, LocalDate.of(1000, 1, 1), LocalDate.now());
        YearMonth now = YearMonth.now();
        Report month = report(uid, now);
        List<BudgetView> budgetViews = budgets.list(uid, now.getMonthValue(), now.getYear());
        BigDecimal allocated = budgetViews.stream().map(BudgetView::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spent = budgetViews.stream().map(BudgetView::spent).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Summary(all.totalIncome(), all.totalExpense(), all.netBalance(), all.netBalance(), FinancialMath.percentage(all.netBalance(), all.totalIncome()), month.totalExpense(), all.transactionCount(), allocated, spent, FinancialMath.percentage(spent, allocated), budgetViews, goals.list(uid));
    }
    public List<MonthlyTotal> monthly(Long uid, int year) {
        DateRanges.month(1, year);
        Map<Integer, BigDecimal[]> values = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) values.put(m, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        for (Object[] row : transactions.monthlyTotals(uid, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))) {
            values.get(((Number) row[1]).intValue())[row[2] == TransactionType.INCOME ? 0 : 1] = (BigDecimal) row[3];
        }
        return values.entrySet().stream().map(e -> new MonthlyTotal(YearMonth.of(year, e.getKey()).toString(), e.getValue()[0], e.getValue()[1])).toList();
    }
}
