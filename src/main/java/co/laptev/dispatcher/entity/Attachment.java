package co.laptev.dispatcher.entity;

import java.io.InputStream;

public class Attachment {
    private final InputStream content;
    private final String name;

    public Attachment(InputStream content, String name) {
        this.content = content;
        this.name = name;
    }

    public InputStream getContent() {
        return content;
    }

    public String getName() {
        return name;
    }
}