package co.laptev.dispatcher.entity;

import java.util.Optional;

public class TaskBuilder {
    private String name;
    private String email;
    private String details;
    private Attachment attachment;

    public TaskBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TaskBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public TaskBuilder withDetails(String details) {
        this.details = details;
        return this;
    }

    public TaskBuilder withAttachment(Attachment attachment) {
        this.attachment = attachment;
        return this;
    }

    public Optional<Task> build() {
        if (name == null || email == null || details == null) {
            return Optional.empty();
        }

        return Optional.of(new Task(name, email, details, Optional.ofNullable(attachment)));
    }
}