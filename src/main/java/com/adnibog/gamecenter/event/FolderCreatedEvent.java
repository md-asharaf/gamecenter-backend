package com.adnibog.gamecenter.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class FolderCreatedEvent extends ApplicationEvent {
    private final String projectId;
    private final String folderId;
    private final boolean isFirstFolder;

    public FolderCreatedEvent(Object source, String projectId, String folderId, boolean isFirstFolder) {
        super(source);
        this.projectId = projectId;
        this.folderId = folderId;
        this.isFirstFolder = isFirstFolder;
    }
}
