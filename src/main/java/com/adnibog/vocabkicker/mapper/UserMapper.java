package com.adnibog.vocabkicker.mapper;

import org.springframework.stereotype.Component;

import com.adnibog.vocabkicker.dto.response.UserDto;
import com.adnibog.vocabkicker.entity.User;

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
