package co.laptev.dispatcher.entity;

import java.util.Optional;

public class Task {
    private final String name;
    private final String email;
    private final String details;
    private final Optional<Attachment> attachment;

    public Task(String name, String email, String details, Optional<Attachment> attachment) {
        this.name = name;
        this.email = email;
        this.details = details;
        this.attachment = attachment;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDetails() {
        return details;
    }

    public Optional<Attachment> getAttachment() {
        return attachment;
    }
}