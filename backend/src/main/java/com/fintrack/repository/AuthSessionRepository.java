package com.fintrack.repository;
import com.fintrack.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
    boolean existsByIdAndUserIdAndExpiresAtAfter(String id, Long userId, Instant now);
    void deleteByExpiresAtBefore(Instant now);
}
