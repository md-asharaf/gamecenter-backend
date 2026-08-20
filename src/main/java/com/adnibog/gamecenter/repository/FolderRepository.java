package com.adnibog.gamecenter.repository;

import java.util.Optional;
import com.adnibog.gamecenter.entity.Folder;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.repository.pagination.FolderPage;

public interface FolderRepository {
  Optional<Folder> findById(String projectId, String folderId);

  void save(Folder folder);

  void deleteById(String projectId, String folderId);

  void deleteAllByProjectId(String projectId);

  boolean hasAnyFolders(String projectId);

  boolean existsByProjectIdAndName(String projectId, String name, String excludeFolderId);

  FolderPage getFolders(String projectId, PaginationRequest req);
}
