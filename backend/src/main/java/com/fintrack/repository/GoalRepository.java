package com.fintrack.repository;
import com.fintrack.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface GoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserIdOrderByDeadlineAsc(Long userId);
    Optional<FinancialGoal> findByIdAndUserId(Long id, Long userId);
}
