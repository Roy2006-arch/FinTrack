package com.fintrack.service;
import com.fintrack.dto.TransactionFilter;
import com.fintrack.dto.Views.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class CsvExportService {
    private final TransactionService transactions;
    public CsvExportService(TransactionService transactions) { this.transactions = transactions; }
    public static String cell(Object value) {
        String s = value == null ? "" : value.toString();
        // Prevent spreadsheet formula execution, including whitespace-prefixed formulas.
        String stripped = s.stripLeading();
        if ((!stripped.isEmpty() && "=+@-".indexOf(stripped.charAt(0)) >= 0) || s.startsWith("\t") || s.startsWith("\r")) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    private void row(Writer writer, Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) { if (i > 0) writer.write(','); writer.write(cell(values[i])); }
        writer.write("\r\n");
    }
    @Transactional(readOnly = true) public void transactions(Long uid, TransactionFilter filter, OutputStream output) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        writer.write('\uFEFF'); row(writer, "ID", "Date", "Type", "Category", "Description", "Payment method", "Amount (INR)");
        int page = 0;
        PageView<TransactionView> result;
        do {
            result = transactions.list(uid, new TransactionFilter(filter.search(), filter.categoryId(), filter.type(), filter.paymentMethod(), filter.start(), filter.end(), filter.sort(), filter.direction(), page++, 100));
            for (TransactionView t : result.content()) row(writer, t.id(), t.transactionDate(), t.type(), t.categoryName(), t.description(), t.paymentMethod(), t.amount());
        } while (page < result.totalPages());
        writer.flush();
    }
    public byte[] report(Report report) throws IOException {
        StringWriter writer = new StringWriter(); writer.write('\uFEFF');
        row(writer, "Metric", "Value"); row(writer, "Start", report.start()); row(writer, "End", report.end());
        row(writer, "Income (INR)", report.totalIncome()); row(writer, "Expense (INR)", report.totalExpense());
        row(writer, "Net balance (INR)", report.netBalance()); row(writer, "Transaction count", report.transactionCount());
        row(writer, "Highest expense (INR)", report.highestExpense()); row(writer, "Average transaction (INR)", report.averageTransaction());
        row(writer, "Expense category", "Amount (INR)");
        for (CategoryTotal category : report.categories()) row(writer, category.categoryName(), category.total());
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
