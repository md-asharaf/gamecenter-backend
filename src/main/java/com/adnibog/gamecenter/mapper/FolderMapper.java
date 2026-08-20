package com.adnibog.gamecenter.mapper;

import org.springframework.stereotype.Component;
import com.adnibog.gamecenter.dto.model.FolderDto;
import com.adnibog.gamecenter.entity.Folder;

@Component
public class FolderMapper {
  public FolderDto toDto(Folder folder) {
    if (folder == null) return null;
    return FolderDto.builder()
        .id(folder.getId())
        .projectId(folder.getProjectId())
        .name(folder.getName())
        .createdAt(folder.getCreatedAt())
        .updatedAt(folder.getUpdatedAt())
        .build();
  }
}
