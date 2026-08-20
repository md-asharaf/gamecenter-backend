package com.adnibog.gamecenter.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateFolderRequest {
  @NotBlank(message = "Folder name is required")
  private String name;
}
