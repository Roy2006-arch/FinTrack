package com.fintrack.controller;
import com.fintrack.dto.*;
import com.fintrack.dto.Requests.TransactionInput;
import com.fintrack.dto.Views.*;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController @RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactions;
    private final CsvExportService exports;
    public TransactionController(TransactionService transactions, CsvExportService exports) { this.transactions = transactions; this.exports = exports; }
    @GetMapping public PageView<TransactionView> list(@AuthenticationPrincipal CurrentUser u, @ModelAttribute TransactionQuery query) { return transactions.list(u.id(), query.filter()); }
    @GetMapping("/{id}") public TransactionView get(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { return transactions.get(u.id(), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public TransactionView create(@AuthenticationPrincipal CurrentUser u, @Valid @RequestBody TransactionInput input) { return transactions.create(u.id(), input); }
    @PutMapping("/{id}") public TransactionView update(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id, @Valid @RequestBody TransactionInput input) { return transactions.update(u.id(), id, input); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { transactions.delete(u.id(), id); }
    @GetMapping("/export") public void export(@AuthenticationPrincipal CurrentUser u, @ModelAttribute TransactionQuery query, HttpServletResponse response) throws IOException {
        transactions.list(u.id(), query.filter());
        response.setContentType("text/csv;charset=UTF-8"); response.setHeader("Content-Disposition", "attachment; filename=fintrack-transactions.csv");
        exports.transactions(u.id(), query.filter(), response.getOutputStream());
    }
}
