package com.adnibog.gamecenter.event;

import org.springframework.context.ApplicationEvent;

public class ProjectDeletedEvent extends ApplicationEvent {
    private final String projectId;

    public ProjectDeletedEvent(Object source, String projectId) {
        super(source);
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }
}
