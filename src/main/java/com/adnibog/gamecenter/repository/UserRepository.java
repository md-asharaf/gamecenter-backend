package com.adnibog.gamecenter.repository;

import java.util.Optional;

import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.repository.pagination.UserPage;

public interface UserRepository {
  Optional<User> findByEmail(String email);

  Optional<User> findById(String id);

  void save(User user);

  void removeProjectFromAllAdmins(String projectId);

  long countAll();

  void deleteById(String id);

  UserPage findUsers(PaginationRequest pageReq);
}
