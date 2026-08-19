package com.adnibog.gamecenter.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdminRequest {
  @NotBlank(message = "Email is required.")
  @Email(message = "Invalid email format.")
  private String email;

  @NotBlank(message = "Password is required.")
  @Size(min = 8, max = 128, message = "Password length must be between 8 and 128 characters.")
  private String password;

  private Set<String> projectIds;
}
