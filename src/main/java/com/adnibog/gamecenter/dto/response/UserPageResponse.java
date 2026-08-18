package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
  private List<UserDto> items;
  private String lastEvaluatedKey;
}
