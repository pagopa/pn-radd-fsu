package it.pagopa.pn.radd.middleware.queue.consumer;

import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;

@CustomLog
public class HandleEventUtils {
    private static final String RADD_ALT_IMPORT_ASYNC = "RADD-ALT IMPORT - ";

    private HandleEventUtils() {
    }

    public static void handleException(MessageHeaders headers, Throwable t) {
        if (headers != null) {
            log.error(RADD_ALT_IMPORT_ASYNC + "Generic exception ex= {}", t.getMessage(), t);
        } else {
            log.error(RADD_ALT_IMPORT_ASYNC + "Generic exception ex ", t);
        }
    }
}
