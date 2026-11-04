package com.fintrack.util;
import java.math.BigDecimal;
import java.math.RoundingMode;
public final class FinancialMath {
    private FinancialMath() {}
    public static BigDecimal percentage(BigDecimal part, BigDecimal whole) {
        return whole.signum() == 0 ? BigDecimal.ZERO : part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
    }
    public static String warning(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) return "EXCEEDED";
        if (percentage.compareTo(BigDecimal.valueOf(90)) >= 0) return "CRITICAL";
        if (percentage.compareTo(BigDecimal.valueOf(75)) >= 0) return "WARNING";
        return "NORMAL";
    }
}
