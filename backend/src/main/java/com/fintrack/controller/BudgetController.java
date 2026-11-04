package com.fintrack.controller;
import com.fintrack.dto.Requests.BudgetInput;
import com.fintrack.dto.Views.BudgetView;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.util.List;
@RestController @RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService budgets;
    public BudgetController(BudgetService budgets) { this.budgets = budgets; }
    @GetMapping public List<BudgetView> list(@AuthenticationPrincipal CurrentUser u, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year) {
        YearMonth now = YearMonth.now(); return budgets.list(u.id(), month == null ? now.getMonthValue() : month, year == null ? now.getYear() : year);
    }
    @GetMapping("/{id}") public BudgetView get(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { return budgets.get(u.id(), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public BudgetView create(@AuthenticationPrincipal CurrentUser u, @Valid @RequestBody BudgetInput input) { return budgets.create(u.id(), input); }
    @PutMapping("/{id}") public BudgetView update(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id, @Valid @RequestBody BudgetInput input) { return budgets.update(u.id(), id, input); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { budgets.delete(u.id(), id); }
}
