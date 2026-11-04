package com.fintrack.service.impl;
import com.fintrack.dto.*;
import com.fintrack.dto.Requests.TransactionInput;
import com.fintrack.dto.Views.*;
import com.fintrack.entity.Transaction;
import com.fintrack.exception.*;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.repository.*;
import com.fintrack.service.*;
import com.fintrack.util.DateRanges;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service @Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactions;
    private final CategoryService categories;
    private final UserService users;
    public TransactionServiceImpl(TransactionRepository transactions, CategoryService categories, UserService users) {
        this.transactions = transactions; this.categories = categories; this.users = users;
    }
    @Override public PageView<TransactionView> list(Long uid, TransactionFilter f) {
        DateRanges.validate(f.start(), f.end());
        if (!Set.of("amount", "transactionDate").contains(f.sort()) || !Set.of("asc", "desc").contains(f.direction())) throw new BadRequestException("Invalid sort or direction");
        if (f.page() < 0 || f.size() < 1 || f.size() > 100 || (f.search() != null && f.search().length() > 100)) throw new BadRequestException("Page must be nonnegative, size 1–100, search at most 100 characters");
        Sort sort = Sort.by(Sort.Direction.fromString(f.direction()), f.sort()).and(Sort.by(Sort.Direction.DESC, "id"));
        Page<Transaction> page = transactions.findAll(TransactionSpecifications.matching(uid, f), PageRequest.of(f.page(), f.size(), sort));
        return new PageView<>(page.getContent().stream().map(FinanceMapper::transaction).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
    private Transaction require(Long uid, Long id) { return transactions.findByIdAndUserId(id, uid).orElseThrow(() -> new ResourceNotFoundException("Transaction")); }
    @Override public TransactionView get(Long uid, Long id) { return FinanceMapper.transaction(require(uid, id)); }
    private void apply(Long uid, Transaction t, TransactionInput input) {
        t.setCategory(categories.require(uid, input.categoryId(), input.type()));
        t.setAmount(input.amount()); t.setType(input.type()); t.setDescription(input.description().strip());
        t.setPaymentMethod(input.paymentMethod()); t.setTransactionDate(input.transactionDate());
    }
    @Override @Transactional public TransactionView create(Long uid, TransactionInput input) {
        Transaction t = new Transaction(); t.setUser(users.require(uid)); apply(uid, t, input);
        return FinanceMapper.transaction(transactions.save(t));
    }
    @Override @Transactional public TransactionView update(Long uid, Long id, TransactionInput input) {
        Transaction t = require(uid, id); apply(uid, t, input);
        return FinanceMapper.transaction(transactions.save(t));
    }
    @Override @Transactional public void delete(Long uid, Long id) { transactions.delete(require(uid, id)); }
}
