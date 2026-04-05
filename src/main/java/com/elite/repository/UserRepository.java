package com.elite.repository;

import com.elite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmailAndPassword(String email, String password);
    List<User> findByNameContainingIgnoreCaseOrHeadlineContainingIgnoreCase(String name, String headline);
}