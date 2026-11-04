package com.fintrack.repository;

import com.fintrack.entity.Transaction;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @EntityGraph(attributePaths = "category")
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
    boolean existsByCategoryId(Long categoryId);
    @Query("select t.type, sum(t.amount), count(t), max(t.amount) from Transaction t where t.user.id = :userId and t.transactionDate between :start and :end group by t.type")
    List<Object[]> totals(Long userId, LocalDate start, LocalDate end);
    @Query("select t.category.id, t.category.name, sum(t.amount) from Transaction t where t.user.id = :userId and t.type = com.fintrack.entity.TransactionType.EXPENSE and t.transactionDate between :start and :end group by t.category.id, t.category.name order by sum(t.amount) desc")
    List<Object[]> categoryTotals(Long userId, LocalDate start, LocalDate end);
    @Query("select year(t.transactionDate), month(t.transactionDate), t.type, sum(t.amount) from Transaction t where t.user.id = :userId and t.transactionDate between :start and :end group by year(t.transactionDate), month(t.transactionDate), t.type")
    List<Object[]> monthlyTotals(Long userId, LocalDate start, LocalDate end);
}
