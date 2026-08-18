package com.adnibog.vocabkicker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
  private String id;
  private String email;
  private Long createdAt;
  private Long updatedAt;
}
