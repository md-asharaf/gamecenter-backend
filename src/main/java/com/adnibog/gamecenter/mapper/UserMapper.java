package com.adnibog.gamecenter.mapper;

import org.springframework.stereotype.Component;

import com.adnibog.gamecenter.dto.model.UserDto;
import com.adnibog.gamecenter.entity.User;

@Component
public class UserMapper {

  public UserDto toDto(User user) {
    if (user == null) {
      return null;
    }
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .projectIds(user.getProjectIds())
        .role(user.getRole())
        .build();
  }
}
