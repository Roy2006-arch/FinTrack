package com.fintrack.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "auth_sessions", indexes = @Index(name = "idx_session_expiry", columnList = "expires_at"))
public class AuthSession {
    @Id @Column(length = 36) private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false) private Instant expiresAt;
    protected AuthSession() {}
    public AuthSession(String id, User user, Instant expiresAt) { this.id = id; this.user = user; this.expiresAt = expiresAt; }
    public String getId() { return id; }
    public User getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
}
