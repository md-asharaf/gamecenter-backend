package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
  private boolean success;
  private String error;
  private List<String> details;

  public static ApiError failure(String error) {
    return new ApiError(false, error, null);
  }

  public static ApiError failure(String error, List<String> details) {
    return new ApiError(false, error, details);
  }
}
