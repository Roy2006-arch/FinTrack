package com.fintrack.service;
import com.fintrack.dto.Requests.CategoryInput;
import com.fintrack.dto.Views.CategoryView;
import com.fintrack.entity.*;
import com.fintrack.exception.*;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private final BudgetRepository budgets;
    private final UserService users;
    public CategoryService(CategoryRepository categories, TransactionRepository transactions, BudgetRepository budgets, UserService users) {
        this.categories = categories; this.transactions = transactions; this.budgets = budgets; this.users = users;
    }
    public List<CategoryView> list(Long uid) { return categories.findByUserIdOrderByNameAsc(uid).stream().map(FinanceMapper::category).toList(); }
    public Category require(Long uid, Long id) { return categories.findByIdAndUserId(id, uid).orElseThrow(() -> new ResourceNotFoundException("Category")); }
    public Category require(Long uid, Long id, TransactionType type) {
        Category category = require(uid, id);
        if (category.getType() != CategoryType.BOTH && !category.getType().name().equals(type.name())) throw new BadRequestException("Category does not support " + type);
        return category;
    }
    @Transactional public CategoryView create(Long uid, CategoryInput input) {
        if (categories.existsByUserIdAndNameIgnoreCase(uid, input.name().strip())) throw new ConflictException("Category name already exists");
        return FinanceMapper.category(categories.save(new Category(users.require(uid), input.name().strip(), input.type())));
    }
    @Transactional public CategoryView update(Long uid, Long id, CategoryInput input) {
        Category category = require(uid, id);
        if (categories.existsByUserIdAndNameIgnoreCaseAndIdNot(uid, input.name().strip(), id)) throw new ConflictException("Category name already exists");
        if (category.getType() != input.type() && (transactions.existsByCategoryId(id) || budgets.existsByCategoryId(id))) throw new ConflictException("Cannot change type of a category in use");
        category.setName(input.name().strip()); category.setType(input.type());
        return FinanceMapper.category(categories.save(category));
    }
    @Transactional public void delete(Long uid, Long id) {
        Category category = require(uid, id);
        if (transactions.existsByCategoryId(id) || budgets.existsByCategoryId(id)) throw new ConflictException("Category is used by transactions or budgets");
        categories.delete(category);
    }
}
