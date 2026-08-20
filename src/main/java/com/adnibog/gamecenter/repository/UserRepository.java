package com.adnibog.gamecenter.repository;

import java.util.List;
import java.util.Optional;

import com.adnibog.gamecenter.entity.User;

public interface UserRepository {
  Optional<User> findByEmail(String email);

  Optional<User> findById(String id);

  void save(User user);

  List<User> findAll();

  long countAll();

  void deleteById(String id);

  UserPage findUsers(int limit, String lastEvaluatedKeyId, String searchKeyword);
}
