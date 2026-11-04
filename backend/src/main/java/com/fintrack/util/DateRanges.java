package com.fintrack.util;
import com.fintrack.exception.BadRequestException;
import java.time.*;
public final class DateRanges {
    private DateRanges() {}
    public static YearMonth month(int month, int year) {
        if (month < 1 || month > 12 || year < 2000 || year > 2100) throw new BadRequestException("Month must be 1–12 and year 2000–2100");
        return YearMonth.of(year, month);
    }
    public static void validate(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) throw new BadRequestException("Start date must not be after end date");
    }
}
