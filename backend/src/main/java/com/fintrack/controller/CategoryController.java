package com.fintrack.controller;
import com.fintrack.dto.Requests.CategoryInput;
import com.fintrack.dto.Views.CategoryView;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categories;
    public CategoryController(CategoryService categories) { this.categories = categories; }
    @GetMapping public List<CategoryView> list(@AuthenticationPrincipal CurrentUser u) { return categories.list(u.id()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public CategoryView create(@AuthenticationPrincipal CurrentUser u, @Valid @RequestBody CategoryInput input) { return categories.create(u.id(), input); }
    @PutMapping("/{id}") public CategoryView update(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id, @Valid @RequestBody CategoryInput input) { return categories.update(u.id(), id, input); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { categories.delete(u.id(), id); }
}
