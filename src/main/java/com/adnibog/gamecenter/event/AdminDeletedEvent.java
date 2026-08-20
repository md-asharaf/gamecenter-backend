package com.adnibog.gamecenter.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class AdminDeletedEvent extends ApplicationEvent {
    private final String adminId;

    public AdminDeletedEvent(Object source, String adminId) {
        super(source);
        this.adminId = adminId;
    }
}
