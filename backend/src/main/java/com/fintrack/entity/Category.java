package com.fintrack.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
public class Category extends BaseEntity {
    @Column(nullable = false, length = 60) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private CategoryType type;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @OneToMany(mappedBy = "category") private List<Transaction> transactions = new ArrayList<>();
    @OneToMany(mappedBy = "category") private List<Budget> budgets = new ArrayList<>();
    public Category() {}
    public Category(User user, String name, CategoryType type) { this.user = user; this.name = name; this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    public User getUser() { return user; }
}
