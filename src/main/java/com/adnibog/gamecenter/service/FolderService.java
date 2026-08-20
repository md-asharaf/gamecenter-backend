package com.adnibog.gamecenter.service;

import com.adnibog.gamecenter.dto.model.FolderDto;
import com.adnibog.gamecenter.dto.request.CreateFolderRequest;
import com.adnibog.gamecenter.dto.request.UpdateFolderRequest;
import com.adnibog.gamecenter.entity.Folder;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.FolderMapper;
import com.adnibog.gamecenter.repository.FolderRepository;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.dto.response.FolderPageResponse;
import com.adnibog.gamecenter.repository.pagination.FolderPage;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import com.adnibog.gamecenter.event.ProjectDeletedEvent;
import com.adnibog.gamecenter.event.FolderDeletedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FolderService {

  private final FolderRepository folderRepository;
  private final FolderMapper folderMapper;
  private final ProjectService projectService;
  private final ApplicationEventPublisher eventPublisher;

  public FolderService(FolderRepository folderRepository, FolderMapper folderMapper,
      ProjectService projectService, ApplicationEventPublisher eventPublisher) {
    this.folderRepository = folderRepository;
    this.folderMapper = folderMapper;
    this.projectService = projectService;
    this.eventPublisher = eventPublisher;
  }

  public FolderPageResponse listFolders(String projectId, PaginationRequest req) {
    projectService.getProjectById(projectId);
    FolderPage page = folderRepository.getFolders(projectId, req);
    List<FolderDto> items = page.getItems().stream()
        .map(folderMapper::toDto)
        .collect(Collectors.toList());
    return new FolderPageResponse(items, page.getLastEvaluatedKey());
  }

  public FolderDto createFolder(String projectId, CreateFolderRequest req) {
    projectService.getProjectById(projectId);

    boolean isFirstFolder = !folderRepository.hasAnyFolders(projectId);

    boolean exists = folderRepository.existsByProjectIdAndName(projectId, req.getName(), null);
    if (exists) {
      throw new ConflictException("Folder with this name already exists in the project");
    }

    Folder folder = Folder.builder()
        .projectId(projectId)
        .id(UUID.randomUUID().toString())
        .name(req.getName())
        .createdAt(System.currentTimeMillis())
        .updatedAt(System.currentTimeMillis())
        .build();

    folderRepository.save(folder);

    if (isFirstFolder) {
      projectService.updateQuizFolderId(projectId, folder.getId());
    }

    return folderMapper.toDto(folder);
  }

  public FolderDto updateFolder(String projectId, String folderId, UpdateFolderRequest req) {
    Folder folder = folderRepository.findById(projectId, folderId)
        .orElseThrow(() -> new NotFoundException("Folder not found"));

    if (!folder.getName().equalsIgnoreCase(req.getName())) {
      boolean exists = folderRepository.existsByProjectIdAndName(projectId, req.getName(), folderId);
      if (exists) {
        throw new ConflictException("Folder with this name already exists in the project");
      }
      folder.setName(req.getName());
      folder.setUpdatedAt(System.currentTimeMillis());
      folderRepository.save(folder);
    }

    return folderMapper.toDto(folder);
  }

  public void deleteFolder(String projectId, String folderId) {
    folderRepository.findById(projectId, folderId)
        .orElseThrow(() -> new NotFoundException("Folder not found"));

    eventPublisher.publishEvent(new FolderDeletedEvent(this, projectId, folderId));
    folderRepository.deleteById(projectId, folderId);

    if (!folderRepository.hasAnyFolders(projectId)) {
      projectService.updateQuizFolderId(projectId, null);
    }
  }

  public void emptyFolder(String projectId, String folderId) {
    folderRepository.findById(projectId, folderId)
        .orElseThrow(() -> new NotFoundException("Folder not found"));

    eventPublisher.publishEvent(new FolderDeletedEvent(this, projectId, folderId));
  }

  public FolderDto getFolderById(String projectId, String folderId) {
    Folder folder = folderRepository.findById(projectId, folderId)
        .orElseThrow(() -> new NotFoundException("Folder not found"));
    return folderMapper.toDto(folder);
  }

  @EventListener
  public void handleProjectDeletedEvent(ProjectDeletedEvent event) {
    log.info("Handling ProjectDeletedEvent in FolderService for project {}", event.getProjectId());
    folderRepository.deleteAllByProjectId(event.getProjectId());
  }
}
