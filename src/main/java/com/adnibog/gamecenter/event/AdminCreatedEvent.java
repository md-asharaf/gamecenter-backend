package com.adnibog.gamecenter.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class AdminCreatedEvent extends ApplicationEvent {
    private final String adminId;

    public AdminCreatedEvent(Object source, String adminId) {
        super(source);
        this.adminId = adminId;
    }
}
