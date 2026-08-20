package com.adnibog.gamecenter.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class ProjectCreatedEvent extends ApplicationEvent {
    private final String projectId;

    public ProjectCreatedEvent(Object source, String projectId) {
        super(source);
        this.projectId = projectId;
    }
}
