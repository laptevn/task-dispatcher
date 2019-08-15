package co.laptev.dispatcher;

import co.laptev.dispatcher.entity.Task;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class TaskDispatcher implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private static final int STATUS_OK = 200;
    private static final int STATUS_SERVER_ERROR = 500;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final String EMAIL_SUBJECT = "$$$ New task request";

    private static final Logger logger = LogManager.getLogger(TaskDispatcher.class);

    private final EmailSender emailSender;
    private final String destinationEmail;
    private final ParameterParser parameterParser;
    private final MessageBuilder messageBuilder;

    public TaskDispatcher() {
        emailSender = new EmailSender(
                getEnvVariable("EMAIL_FROM"),
                getEnvVariable("EMAIL_USER_NAME"),
                getEnvVariable("EMAIL_PASSWORD"));

        destinationEmail = getEnvVariable("EMAIL_TO");
        parameterParser = new ParameterParser();
        messageBuilder = new MessageBuilder();
    }

    private static String getEnvVariable(String key) {
        String value = System.getenv(key);
        if (value == null) {
            String errorMessage = String.format("'%s' environment variable is not set", key);
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return value;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Optional<Task> parsedTask = parameterParser.parse(input);
        int statusCode = parsedTask
                .map(task -> emailSender.send(
                        destinationEmail, EMAIL_SUBJECT, messageBuilder.build(task), task.getAttachment())
                        ? STATUS_OK
                        : STATUS_SERVER_ERROR)
                .orElse(STATUS_BAD_REQUEST);

        logger.info("Handle request. Status code = {}", statusCode);

        return Collections.singletonMap("statusCode", statusCode);
    }
}