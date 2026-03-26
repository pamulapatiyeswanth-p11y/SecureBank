package com.securebank.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securebank.user_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA auto-implements this from the method name
    Optional<User> findByEmail(String email);
    
     // Useful for registration validation
    boolean existsByEmail(String email);   

     // Useful for phone validation
    boolean existsByPhoneNumber(String phoneNumber);
}
