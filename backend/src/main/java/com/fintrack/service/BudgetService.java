package com.fintrack.service;
import com.fintrack.dto.Requests.BudgetInput;
import com.fintrack.dto.Views.BudgetView;
import com.fintrack.entity.*;
import com.fintrack.exception.*;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.repository.*;
import com.fintrack.util.DateRanges;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service @Transactional(readOnly = true)
public class BudgetService {
    private final BudgetRepository budgets;
    private final TransactionRepository transactions;
    private final CategoryService categories;
    private final UserService users;
    public BudgetService(BudgetRepository budgets, TransactionRepository transactions, CategoryService categories, UserService users) {
        this.budgets = budgets; this.transactions = transactions; this.categories = categories; this.users = users;
    }
    public List<BudgetView> list(Long uid, int month, int year) {
        YearMonth period = DateRanges.month(month, year);
        Map<Long, BigDecimal> totals = spent(uid, period);
        return budgets.findByUserIdAndMonthAndYearOrderByIdDesc(uid, month, year).stream().map(b -> FinanceMapper.budget(b, totals.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO))).toList();
    }
    private Map<Long, BigDecimal> spent(Long uid, YearMonth period) {
        return transactions.categoryTotals(uid, period.atDay(1), period.atEndOfMonth()).stream().collect(Collectors.toMap(r -> (Long) r[0], r -> (BigDecimal) r[2]));
    }
    private Budget require(Long uid, Long id) { return budgets.findByIdAndUserId(id, uid).orElseThrow(() -> new ResourceNotFoundException("Budget")); }
    private BudgetView view(Long uid, Budget b) { return FinanceMapper.budget(b, spent(uid, YearMonth.of(b.getYear(), b.getMonth())).getOrDefault(b.getCategory().getId(), BigDecimal.ZERO)); }
    public BudgetView get(Long uid, Long id) { return view(uid, require(uid, id)); }
    private void apply(Long uid, Budget b, BudgetInput input) {
        DateRanges.month(input.month(), input.year());
        b.setCategory(categories.require(uid, input.categoryId(), TransactionType.EXPENSE)); b.setAmount(input.amount()); b.setMonth(input.month()); b.setYear(input.year());
    }
    @Transactional public BudgetView create(Long uid, BudgetInput input) {
        if (budgets.existsByUserIdAndCategoryIdAndMonthAndYear(uid, input.categoryId(), input.month(), input.year())) throw new ConflictException("Budget already exists for this category and month");
        Budget b = new Budget(); b.setUser(users.require(uid)); apply(uid, b, input);
        return view(uid, budgets.save(b));
    }
    @Transactional public BudgetView update(Long uid, Long id, BudgetInput input) {
        Budget b = require(uid, id);
        if (budgets.existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(uid, input.categoryId(), input.month(), input.year(), id)) throw new ConflictException("Budget already exists for this category and month");
        apply(uid, b, input); return view(uid, budgets.save(b));
    }
    @Transactional public void delete(Long uid, Long id) { budgets.delete(require(uid, id)); }
}
