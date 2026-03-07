package org.example.event_platform.Repository;

import org.example.event_platform.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    // Dùng để Login & Filter JWT
    Optional<User> findByUsername(String username);

    // Dùng cho endpoint /me (Lấy User kèm theo Tenant và Roles để tránh LazyInitializationException)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findUserWithDetailsById(Long id);

    // Kiểm tra nhanh trạng thái trước khi cho Login
    boolean existsByUsernameAndIsActiveTrue(String username);
}