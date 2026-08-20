package com.adnibog.gamecenter.dto.response;

import java.util.List;
import com.adnibog.gamecenter.dto.model.FolderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderPageResponse {
  private List<FolderDto> items;
  private String lastEvaluatedKey;
}
