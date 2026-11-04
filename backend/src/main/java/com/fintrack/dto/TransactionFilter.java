package com.fintrack.dto;

import com.fintrack.entity.*;
import java.time.LocalDate;

public record TransactionFilter(String search, Long categoryId, TransactionType type,
    PaymentMethod paymentMethod, LocalDate start, LocalDate end, String sort, String direction, int page, int size) {}
