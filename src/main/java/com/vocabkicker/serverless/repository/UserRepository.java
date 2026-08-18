package com.vocabkicker.serverless.repository;

import com.vocabkicker.serverless.entity.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
    void save(User user);
    long count();
    java.util.List<User> findAll();
    void deleteById(String id);
}
