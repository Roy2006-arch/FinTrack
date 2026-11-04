package com.fintrack.mapper;
import com.fintrack.dto.Views.*;
import com.fintrack.entity.*;
import com.fintrack.util.FinancialMath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class FinanceMapper {
    private FinanceMapper() {}
    public static UserView user(User u) { return new UserView(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getCreatedAt()); }
    public static CategoryView category(Category c) { return new CategoryView(c.getId(), c.getName(), c.getType()); }
    public static TransactionView transaction(Transaction t) {
        return new TransactionView(t.getId(), t.getAmount(), t.getType(), t.getCategory().getId(), t.getCategory().getName(), t.getDescription(), t.getPaymentMethod(), t.getTransactionDate());
    }
    public static BudgetView budget(Budget b, BigDecimal spent) {
        BigDecimal usage = FinancialMath.percentage(spent, b.getAmount());
        return new BudgetView(b.getId(), b.getCategory().getId(), b.getCategory().getName(), b.getAmount(), b.getMonth(), b.getYear(), spent, b.getAmount().subtract(spent), usage, FinancialMath.warning(usage));
    }
    public static GoalView goal(FinancialGoal g) {
        return new GoalView(g.getId(), g.getGoalName(), g.getTargetAmount(), g.getSavedAmount(), g.getDeadline(), FinancialMath.percentage(g.getSavedAmount(), g.getTargetAmount()), g.getTargetAmount().subtract(g.getSavedAmount()).max(BigDecimal.ZERO), ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline()));
    }
}
