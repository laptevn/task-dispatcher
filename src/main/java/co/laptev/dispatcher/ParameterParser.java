package co.laptev.dispatcher;

import co.laptev.dispatcher.entity.Attachment;
import co.laptev.dispatcher.entity.Task;
import co.laptev.dispatcher.entity.TaskBuilder;
import delight.fileupload.FileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ParameterParser {
    private static final String BODY_ELEMENT = "body";
    private static final String CONTENT_TYPE_ELEMENT = "content-type";
    private static final String NAME_PARAMETER = "name";
    private static final String EMAIL_PARAMETER = "email";
    private static final String DETAILS_PARAMETER = "details";
    private static final String ATTACHED_DETAILS_PARAMETER = "attached-details";
    private static final String ERROR_MISSING_ELEMENT = "Request doesn't contain {} element. Wrong API Gateway configuration.";

    private static final Logger logger = LogManager.getLogger(ParameterParser.class);

    public Optional<Task> parse(Map<String, Object> input) {
        if (!input.containsKey(BODY_ELEMENT)) {
            logger.info(ERROR_MISSING_ELEMENT, BODY_ELEMENT);
            return Optional.empty();
        }

        if (!input.containsKey(CONTENT_TYPE_ELEMENT)) {
            logger.info(ERROR_MISSING_ELEMENT, CONTENT_TYPE_ELEMENT);
            return Optional.empty();
        }

        TaskBuilder taskBuilder = new TaskBuilder();
        List<FileItem> items = FileUpload.parse(
                Base64.getDecoder().decode(input.get(BODY_ELEMENT).toString()),
                input.get(CONTENT_TYPE_ELEMENT).toString());
        for (FileItem item : items) {
            if (item.isFormField()) {
                switch (item.getFieldName()) {
                    case NAME_PARAMETER:
                        taskBuilder.withName(item.getString());
                        break;

                    case EMAIL_PARAMETER:
                        taskBuilder.withEmail(item.getString());
                        break;

                    case DETAILS_PARAMETER:
                        taskBuilder.withDetails(item.getString());
                        break;

                    default:
                        handleUnsupportedParameter(item.getFieldName());
                }
            } else if (item.getFieldName().equals(ATTACHED_DETAILS_PARAMETER)) {
                try {
                    taskBuilder.withAttachment(new Attachment(item.getInputStream(), item.getName()));
                } catch (IOException e) {
                    logger.error("Could not open file passed as parameter", e);
                    return Optional.empty();
                }
            } else {
                handleUnsupportedParameter(item.getFieldName());
            }
        }

        return taskBuilder.build();
    }

    private static void handleUnsupportedParameter(String parameterName) {
        throw new UnsupportedOperationException(String.format("'%s' parameter is not supported", parameterName));
    }
}