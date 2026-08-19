package com.adnibog.gamecenter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank(message = "Password is required.")
  @Size(min = 6, message = "Password length must be at least 6 characters.")
  private String password;
}
