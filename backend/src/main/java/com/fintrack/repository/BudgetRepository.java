package com.fintrack.repository;
import com.fintrack.entity.Budget;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.Optional;
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    @EntityGraph(attributePaths = "category")
    List<Budget> findByUserIdAndMonthAndYearOrderByIdDesc(Long userId, int month, int year);
    @EntityGraph(attributePaths = "category")
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, int month, int year);
    boolean existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(Long userId, Long categoryId, int month, int year, Long id);
}
