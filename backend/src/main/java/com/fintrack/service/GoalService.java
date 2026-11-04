package com.fintrack.service;
import com.fintrack.dto.Requests.GoalInput;
import com.fintrack.dto.Views.GoalView;
import com.fintrack.entity.FinancialGoal;
import com.fintrack.exception.*;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.repository.GoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional(readOnly = true)
public class GoalService {
    private final GoalRepository goals;
    private final UserService users;
    public GoalService(GoalRepository goals, UserService users) { this.goals = goals; this.users = users; }
    private FinancialGoal require(Long uid, Long id) { return goals.findByIdAndUserId(id, uid).orElseThrow(() -> new ResourceNotFoundException("Goal")); }
    public List<GoalView> list(Long uid) { return goals.findByUserIdOrderByDeadlineAsc(uid).stream().map(FinanceMapper::goal).toList(); }
    public GoalView get(Long uid, Long id) { return FinanceMapper.goal(require(uid, id)); }
    private void apply(FinancialGoal goal, GoalInput input) {
        if (input.savedAmount().compareTo(input.targetAmount()) > 0) throw new BadRequestException("Saved amount must not exceed target amount");
        if (input.deadline().getYear() < 2000 || input.deadline().getYear() > 2100) throw new BadRequestException("Deadline year must be 2000–2100");
        goal.setGoalName(input.goalName().strip()); goal.setTargetAmount(input.targetAmount()); goal.setSavedAmount(input.savedAmount()); goal.setDeadline(input.deadline());
    }
    @Transactional public GoalView create(Long uid, GoalInput input) {
        FinancialGoal goal = new FinancialGoal(); goal.setUser(users.require(uid)); apply(goal, input);
        return FinanceMapper.goal(goals.save(goal));
    }
    @Transactional public GoalView update(Long uid, Long id, GoalInput input) {
        FinancialGoal goal = require(uid, id); apply(goal, input); return FinanceMapper.goal(goals.save(goal));
    }
    @Transactional public void delete(Long uid, Long id) { goals.delete(require(uid, id)); }
}
