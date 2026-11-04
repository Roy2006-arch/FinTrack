package com.fintrack.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, unique = true, length = 254) private String email;
    @Column(nullable = false, length = 60) private String password;
    @Column(length = 20) private String phone;
    @OneToMany(mappedBy = "user") private List<Transaction> transactions = new ArrayList<>();
    @OneToMany(mappedBy = "user") private List<Category> categories = new ArrayList<>();
    @OneToMany(mappedBy = "user") private List<Budget> budgets = new ArrayList<>();
    @OneToMany(mappedBy = "user") private List<FinancialGoal> goals = new ArrayList<>();
    public User() {}
    public User(String name, String email, String password) {
        this.name = name; this.email = email; this.password = password;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<Transaction> getTransactions() { return List.copyOf(transactions); }
    public List<Category> getCategories() { return List.copyOf(categories); }
    public List<Budget> getBudgets() { return List.copyOf(budgets); }
    public List<FinancialGoal> getGoals() { return List.copyOf(goals); }
}
