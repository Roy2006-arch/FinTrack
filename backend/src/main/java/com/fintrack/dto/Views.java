package com.fintrack.dto;

import com.fintrack.entity.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class Views {
    private Views() {}
    public record UserView(Long id, String name, String email, String phone, Instant createdAt) {}
    public record AuthView(String token, Instant expiresAt, UserView user) {}
    public record CategoryView(Long id, String name, CategoryType type) {}
    public record TransactionView(Long id, BigDecimal amount, TransactionType type, Long categoryId,
        String categoryName, String description, PaymentMethod paymentMethod, LocalDate transactionDate) {}
    public record PageView<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
    public record BudgetView(Long id, Long categoryId, String categoryName, BigDecimal amount, int month,
        int year, BigDecimal spent, BigDecimal remaining, BigDecimal usagePercentage, String warningLevel) {}
    public record GoalView(Long id, String goalName, BigDecimal targetAmount, BigDecimal savedAmount,
        LocalDate deadline, BigDecimal progressPercentage, BigDecimal remainingAmount, long daysRemaining) {}
    public record CategoryTotal(Long categoryId, String categoryName, BigDecimal total) {}
    public record MonthlyTotal(String month, BigDecimal income, BigDecimal expense) {}
    public record Report(LocalDate start, LocalDate end, BigDecimal totalIncome, BigDecimal totalExpense,
        BigDecimal netBalance, long transactionCount, BigDecimal highestExpense,
        BigDecimal averageTransaction, List<CategoryTotal> categories) {}
    public record Summary(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal balance,
        BigDecimal savings, BigDecimal savingsRate, BigDecimal currentMonthSpending, long transactionCount,
        BigDecimal budgetAmount, BigDecimal budgetSpent, BigDecimal budgetUsage,
        List<BudgetView> budgets, List<GoalView> goals) {}
}
