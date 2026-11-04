package com.fintrack.service;
import com.fintrack.dto.*;
import com.fintrack.dto.Requests.TransactionInput;
import com.fintrack.dto.Views.*;
public interface TransactionService {
    PageView<TransactionView> list(Long userId, TransactionFilter filter);
    TransactionView get(Long userId, Long id);
    TransactionView create(Long userId, TransactionInput input);
    TransactionView update(Long userId, Long id, TransactionInput input);
    void delete(Long userId, Long id);
}
