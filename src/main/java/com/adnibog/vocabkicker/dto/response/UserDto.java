package com.adnibog.vocabkicker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import com.adnibog.vocabkicker.entity.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
  private String id;
  private String email;
  private Long createdAt;
  private Long updatedAt;
  private Set<String> projectIds;
  private Role role;
}
