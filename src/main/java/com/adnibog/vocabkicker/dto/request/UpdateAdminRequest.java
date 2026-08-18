package com.adnibog.vocabkicker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAdminRequest {
  private String email;
  private String password;

  @AssertTrue(message = "At least one field (email, password) must be provided")
  public boolean isAtLeastOneFieldProvided() {
    return (email != null && !email.isBlank()) || (password != null && !password.isBlank());
  }
}
