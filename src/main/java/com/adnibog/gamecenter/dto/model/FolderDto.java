package com.adnibog.gamecenter.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderDto {
  private String id;
  private String projectId;
  private String name;
  private Long createdAt;
  private Long updatedAt;
}
