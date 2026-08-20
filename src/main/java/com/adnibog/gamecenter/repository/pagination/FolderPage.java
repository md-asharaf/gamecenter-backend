package com.adnibog.gamecenter.repository.pagination;

import java.util.List;
import com.adnibog.gamecenter.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderPage {
  private List<Folder> items;
  private String lastEvaluatedKey;
}
