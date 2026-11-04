package com.fintrack.config;
import com.fintrack.dto.Requests.*;
import com.fintrack.dto.Views.*;
import com.fintrack.entity.*;
import com.fintrack.repository.UserRepository;
import com.fintrack.security.JwtService;
import com.fintrack.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component @ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final AuthService auth;
    private final CategoryService categories;
    private final TransactionService transactions;
    private final BudgetService budgets;
    private final GoalService goals;
    private final JwtService jwt;
    private final String password;
    public DemoDataSeeder(UserRepository users, AuthService auth, CategoryService categories, TransactionService transactions, BudgetService budgets, GoalService goals, JwtService jwt, @Value("${app.demo.password}") String password) {
        this.users = users; this.auth = auth; this.categories = categories; this.transactions = transactions; this.budgets = budgets; this.goals = goals; this.jwt = jwt; this.password = password;
    }
    @Override @Transactional public void run(String... args) {
        if (users.existsByEmail("roykaustav382@gmail.com")) return;
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[\\x21-\\x7E]{10,72}$")) throw new IllegalArgumentException("Set DEMO_PASSWORD to a valid 10–72 character password before enabling demo data");
        AuthView registered = auth.register(new Register("Kaustav Roy", "roykaustav382@gmail.com", password));
        Long uid = registered.user().id(); auth.logout(jwt.parse(registered.token()));
        Map<String, Long> ids = categories.list(uid).stream().collect(Collectors.toMap(CategoryView::name, CategoryView::id));
        for (int ago = 5; ago >= 0; ago--) {
            LocalDate first = YearMonth.now().minusMonths(ago).atDay(1);
            add(uid, ids, "Salary", 55000 + ago * 500, "Monthly salary · demo", first, TransactionType.INCOME, PaymentMethod.BANK_TRANSFER);
            add(uid, ids, "Freelance", 8000 + ago * 300, "Design project · demo", first, TransactionType.INCOME, PaymentMethod.BANK_TRANSFER);
            add(uid, ids, "Food", 3800 + ago * 120, "Groceries and dining · demo", first, TransactionType.EXPENSE, PaymentMethod.UPI);
            add(uid, ids, "Transport", 1850 + ago * 90, "Metro and commute · demo", first, TransactionType.EXPENSE, PaymentMethod.CARD);
            add(uid, ids, "Shopping", 4200 + ago * 150, "Home essentials · demo", first, TransactionType.EXPENSE, PaymentMethod.CARD);
            add(uid, ids, "Bills", 9500 + ago * 200, "Rent and utilities · demo", first, TransactionType.EXPENSE, PaymentMethod.BANK_TRANSFER);
            add(uid, ids, "Other", 650 + ago * 50, "Books and stationery · demo", first, TransactionType.EXPENSE, PaymentMethod.CASH);
        }
        YearMonth now = YearMonth.now();
        budgets.create(uid, new BudgetInput(ids.get("Food"), BigDecimal.valueOf(5000), now.getMonthValue(), now.getYear()));
        budgets.create(uid, new BudgetInput(ids.get("Transport"), BigDecimal.valueOf(3000), now.getMonthValue(), now.getYear()));
        budgets.create(uid, new BudgetInput(ids.get("Shopping"), BigDecimal.valueOf(4000), now.getMonthValue(), now.getYear()));
        budgets.create(uid, new BudgetInput(ids.get("Bills"), BigDecimal.valueOf(10000), now.getMonthValue(), now.getYear()));
        goals.create(uid, new GoalInput("New laptop", BigDecimal.valueOf(80000), BigDecimal.valueOf(45000), LocalDate.now().plusMonths(6)));
        goals.create(uid, new GoalInput("Emergency fund", BigDecimal.valueOf(150000), BigDecimal.valueOf(60000), LocalDate.now().plusYears(1)));
        goals.create(uid, new GoalInput("Mountain getaway", BigDecimal.valueOf(30000), BigDecimal.valueOf(12000), LocalDate.now().plusMonths(4)));
    }
    private void add(Long uid, Map<String, Long> ids, String category, long amount, String description, LocalDate date, TransactionType type, PaymentMethod method) {
        transactions.create(uid, new TransactionInput(BigDecimal.valueOf(amount), type, ids.get(category), description, method, date));
    }
}
