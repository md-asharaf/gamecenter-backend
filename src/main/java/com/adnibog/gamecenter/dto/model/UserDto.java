package com.adnibog.gamecenter.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import com.adnibog.gamecenter.entity.Role;

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
