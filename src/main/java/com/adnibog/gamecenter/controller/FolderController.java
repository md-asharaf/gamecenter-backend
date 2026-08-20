package com.adnibog.gamecenter.controller;

import com.adnibog.gamecenter.dto.model.FolderDto;
import com.adnibog.gamecenter.dto.request.CreateFolderRequest;
import com.adnibog.gamecenter.dto.request.UpdateFolderRequest;
import com.adnibog.gamecenter.service.FolderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.dto.response.FolderPageResponse;

@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/folders")
public class FolderController {

  private final FolderService folderService;

  public FolderController(FolderService folderService) {
    this.folderService = folderService;
  }

  @GetMapping
  public ResponseEntity<FolderPageResponse> listFolders(
      @PathVariable String projectId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey) {
    log.info("Request to list folders for project {} with limit {}", projectId, limit);
    PaginationRequest req = new PaginationRequest();
    req.setLimit(limit);
    req.setLastEvaluatedKey(lastEvaluatedKey);
    return ResponseEntity.ok(folderService.listFolders(projectId, req));
  }

  @PostMapping
  public ResponseEntity<FolderDto> createFolder(@PathVariable String projectId, @Valid @RequestBody CreateFolderRequest req) {
    log.info("Request to create folder in project {}: {}", projectId, req.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(folderService.createFolder(projectId, req));
  }

  @GetMapping("/{folderId}")
  public ResponseEntity<FolderDto> getFolder(@PathVariable String projectId, @PathVariable String folderId) {
    return ResponseEntity.ok(folderService.getFolderById(projectId, folderId));
  }

  @PutMapping("/{folderId}")
  public ResponseEntity<FolderDto> updateFolder(@PathVariable String projectId, @PathVariable String folderId, @Valid @RequestBody UpdateFolderRequest req) {
    log.info("Request to update folder {} in project {}", folderId, projectId);
    return ResponseEntity.ok(folderService.updateFolder(projectId, folderId, req));
  }

  @DeleteMapping("/{folderId}")
  public ResponseEntity<Void> deleteFolder(@PathVariable String projectId, @PathVariable String folderId) {
    log.info("Request to delete folder {} in project {}", folderId, projectId);
    folderService.deleteFolder(projectId, folderId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{folderId}/empty")
  public ResponseEntity<Void> emptyFolder(@PathVariable String projectId, @PathVariable String folderId) {
    log.info("Request to empty folder {} in project {}", folderId, projectId);
    folderService.emptyFolder(projectId, folderId);
    return ResponseEntity.noContent().build();
  }
}
