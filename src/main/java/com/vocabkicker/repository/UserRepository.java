package com.vocabkicker.repository;

import com.vocabkicker.entity.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
    void save(User user);
    long count();
    java.util.List<User> findAll();
    void deleteById(String id);
}
