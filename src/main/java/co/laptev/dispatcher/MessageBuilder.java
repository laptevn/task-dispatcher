package co.laptev.dispatcher;

import co.laptev.dispatcher.entity.Task;

class MessageBuilder {
    private static final String HTML_NEW_LINE = "<br>";

    public String build(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append(task.getName()).append(" submitted a task.").append(HTML_NEW_LINE);
        builder.append("Contact email: ").append(task.getEmail()).append(HTML_NEW_LINE);
        builder.append("Details: ").append(task.getDetails());
        return builder.toString();
    }
}