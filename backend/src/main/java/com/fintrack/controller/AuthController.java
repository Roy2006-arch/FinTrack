package com.fintrack.controller;
import com.fintrack.dto.Requests.*;
import com.fintrack.dto.Views.AuthView;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public AuthView register(@Valid @RequestBody Register input) { return auth.register(input); }
    @PostMapping("/login") public AuthView login(@Valid @RequestBody Login input) { return auth.login(input); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(@AuthenticationPrincipal CurrentUser user) { auth.logout(user); }
}
