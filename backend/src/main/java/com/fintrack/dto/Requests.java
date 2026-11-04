package com.fintrack.dto;

import com.fintrack.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Immutable request DTOs keep validation at the API boundary. */
public final class Requests {
    private Requests() {}
    public record Register(@NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 72) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[\\x21-\\x7E]{10,72}$", message = "must have 10–72 ASCII characters, an uppercase letter, lowercase letter and digit") String password) {}
    public record Login(@NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 72) String password) {}
    public record Profile(@NotBlank @Size(max = 100) String name, @NotBlank @Email @Size(max = 254) String email,
        @Pattern(regexp = "^[+0-9 ()-]{0,20}$", message = "must be a valid phone number") String phone) {}
    public record CategoryInput(@NotBlank @Size(max = 60) String name, @NotNull CategoryType type) {}
    public record TransactionInput(@NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal amount,
        @NotNull TransactionType type, @NotNull @Positive Long categoryId,
        @NotNull @Size(max = 500) String description, @NotNull PaymentMethod paymentMethod,
        @NotNull @PastOrPresent LocalDate transactionDate) {}
    public record BudgetInput(@NotNull @Positive Long categoryId,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal amount,
        @Min(1) @Max(12) int month, @Min(2000) @Max(2100) int year) {}
    public record GoalInput(@NotBlank @Size(max = 100) String goalName,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal targetAmount,
        @NotNull @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal savedAmount,
        @NotNull LocalDate deadline) {}
}
