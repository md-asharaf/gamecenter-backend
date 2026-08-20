package com.adnibog.gamecenter.event;

import org.springframework.context.ApplicationEvent;

public class FolderDeletedEvent extends ApplicationEvent {
    private final String projectId;
    private final String folderId;

    public FolderDeletedEvent(Object source, String projectId, String folderId) {
        super(source);
        this.projectId = projectId;
        this.folderId = folderId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getFolderId() {
        return folderId;
    }
}
