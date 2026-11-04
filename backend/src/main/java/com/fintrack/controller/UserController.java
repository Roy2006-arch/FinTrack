package com.fintrack.controller;
import com.fintrack.dto.Requests.Profile;
import com.fintrack.dto.Views.UserView;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/users/me")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }
    @GetMapping public UserView get(@AuthenticationPrincipal CurrentUser u) { return users.get(u.id()); }
    @PutMapping public UserView update(@AuthenticationPrincipal CurrentUser u, @Valid @RequestBody Profile input) { return users.update(u.id(), input); }
}
