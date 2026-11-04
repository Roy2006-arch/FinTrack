package com.fintrack.controller;
import com.fintrack.dto.Requests.GoalInput;
import com.fintrack.dto.Views.GoalView;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goals;
    public GoalController(GoalService goals) { this.goals = goals; }
    @GetMapping public List<GoalView> list(@AuthenticationPrincipal CurrentUser u) { return goals.list(u.id()); }
    @GetMapping("/{id}") public GoalView get(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { return goals.get(u.id(), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public GoalView create(@AuthenticationPrincipal CurrentUser u, @Valid @RequestBody GoalInput input) { return goals.create(u.id(), input); }
    @PutMapping("/{id}") public GoalView update(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id, @Valid @RequestBody GoalInput input) { return goals.update(u.id(), id, input); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@AuthenticationPrincipal CurrentUser u, @PathVariable Long id) { goals.delete(u.id(), id); }
}
