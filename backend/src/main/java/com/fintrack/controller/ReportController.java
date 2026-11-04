package com.fintrack.controller;
import com.fintrack.dto.Views.*;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.*;
import com.fintrack.util.DateRanges;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.*;
import java.util.List;
@RestController @RequestMapping("/api/reports")
public class ReportController {
    private final AnalyticsService analytics;
    private final CsvExportService exports;
    public ReportController(AnalyticsService analytics, CsvExportService exports) { this.analytics = analytics; this.exports = exports; }
    @GetMapping("/monthly") public Report monthly(@AuthenticationPrincipal CurrentUser u, @RequestParam int month, @RequestParam int year) { return analytics.report(u.id(), DateRanges.month(month, year)); }
    @GetMapping("/date-range") public Report range(@AuthenticationPrincipal CurrentUser u, @RequestParam LocalDate start, @RequestParam LocalDate end) { return analytics.report(u.id(), start, end); }
    @GetMapping("/category") public List<CategoryTotal> categories(@AuthenticationPrincipal CurrentUser u, @RequestParam LocalDate start, @RequestParam LocalDate end) { return analytics.categories(u.id(), start, end); }
    @GetMapping("/export") public ResponseEntity<byte[]> export(@AuthenticationPrincipal CurrentUser u, @RequestParam LocalDate start, @RequestParam LocalDate end) throws IOException {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fintrack-report.csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(exports.report(analytics.report(u.id(), start, end)));
    }
}
