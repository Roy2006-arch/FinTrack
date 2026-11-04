package com.fintrack.service;
import com.fintrack.dto.Requests.Profile;
import com.fintrack.dto.Views.UserView;
import com.fintrack.entity.User;
import com.fintrack.exception.*;
import com.fintrack.mapper.FinanceMapper;
import com.fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service @Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }
    public User require(Long id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User")); }
    public UserView get(Long id) { return FinanceMapper.user(require(id)); }
    @Transactional public UserView update(Long id, Profile input) {
        String email = input.email().strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmailAndIdNot(email, id)) throw new ConflictException("Email is already registered");
        User user = require(id);
        user.setName(input.name().strip()); user.setEmail(email); user.setPhone(input.phone());
        return FinanceMapper.user(users.save(user));
    }
}
