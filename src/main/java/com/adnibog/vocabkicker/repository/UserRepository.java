package com.adnibog.vocabkicker.repository;

import java.util.Optional;

import com.adnibog.vocabkicker.entity.User;

public interface UserRepository {
  Optional<User> findByEmail(String email);

  Optional<User> findById(String id);

  void save(User user);

  long count();

  java.util.List<User> findAll();

  void deleteById(String id);
}
