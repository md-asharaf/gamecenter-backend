package com.adnibog.gamecenter.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;
import java.util.Set;

import com.adnibog.gamecenter.entity.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAdminRequest {
  private String email;
  private String password;
  private Set<String> projectIds;

  private Role role;

  @AssertTrue(message = "At least one update field is required.")
  public boolean isAtLeastOneFieldProvided() {
    return (email != null && !email.isBlank()) ||
        (password != null && !password.isBlank()) ||
        (projectIds != null) ||
        (role != null);
  }
}
