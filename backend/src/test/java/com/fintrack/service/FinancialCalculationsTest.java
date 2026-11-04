package com.fintrack.service;
import com.fintrack.entity.*;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.util.FinancialMath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FinancialCalculationsTest {
    @ParameterizedTest @CsvSource({"0,NORMAL", "74.99,NORMAL", "75,WARNING", "89.99,WARNING", "90,CRITICAL", "100,CRITICAL", "100.01,EXCEEDED"})
    void budgetThresholdsHaveCorrectBoundaries(String usage, String warning) { assertEquals(warning, FinancialMath.warning(new BigDecimal(usage))); }
    @Test void budgetCalculatesActualRemainingAndUsage() {
        Budget budget = new Budget(); budget.setCategory(new Category(new User(), "Food", CategoryType.EXPENSE)); budget.setAmount(new BigDecimal("5000"));
        var view = FinanceMapper.budget(budget, new BigDecimal("3800"));
        assertEquals(new BigDecimal("1200"), view.remaining()); assertEquals(new BigDecimal("76.00"), view.usagePercentage()); assertEquals("WARNING", view.warningLevel());
    }
    @Test void goalCalculatesProgressRemainingAndOverdueDays() {
        FinancialGoal goal = new FinancialGoal(); goal.setTargetAmount(new BigDecimal("80000")); goal.setSavedAmount(new BigDecimal("45000")); goal.setDeadline(LocalDate.now().minusDays(2));
        var view = FinanceMapper.goal(goal);
        assertEquals(new BigDecimal("56.25"), view.progressPercentage()); assertEquals(new BigDecimal("35000"), view.remainingAmount()); assertEquals(-2, view.daysRemaining());
    }
    @Test void zeroIncomeAndNegativeSavingsAreHandled() {
        assertEquals(BigDecimal.ZERO, FinancialMath.percentage(new BigDecimal("-100"), BigDecimal.ZERO));
        assertEquals(new BigDecimal("-25.00"), FinancialMath.percentage(new BigDecimal("-250"), new BigDecimal("1000")));
    }
    @Test void csvEscapesQuotesNewlinesAndSpreadsheetFormulas() {
        assertEquals("\"' =SUM(A1:A2)\"", CsvExportService.cell(" =SUM(A1:A2)"));
        assertEquals("\"'@formula\nnext\"", CsvExportService.cell("@formula\nnext"));
        assertEquals("\"Lunch, \"\"friends\"\"\"", CsvExportService.cell("Lunch, \"friends\""));
    }
}
