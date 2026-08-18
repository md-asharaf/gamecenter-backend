package com.adnibog.vocabkicker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAdminRequest {
  private String email;
  private String password;
  private Set<String> projectIds;

  @AssertTrue(message = "At least one field (email, password, projectIds) must be provided")
  public boolean isAtLeastOneFieldProvided() {
    return (email != null && !email.isBlank()) || 
           (password != null && !password.isBlank()) ||
           (projectIds != null);
  }
}
