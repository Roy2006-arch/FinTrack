package com.fintrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "financial_goals")
public class FinancialGoal extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false, length = 100) private String goalName;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal targetAmount;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal savedAmount;
    @Column(nullable = false) private LocalDate deadline;
    public FinancialGoal() {}
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}
