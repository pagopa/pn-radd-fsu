package it.pagopa.pn.radd.utils.log;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.radd.exception.RaddGenericException;
import lombok.Getter;

public class PnRaddAltAuditLog {
    private final PnAuditLogEvent logEvent;

    @Getter
    private final PnRaddAltLogContext context;

    public PnRaddAltAuditLog(PnAuditLogEventType logEventType, String msg, PnRaddAltLogContext context) {
        this.context = context;
        String enrichedMsg = msg + " - " + context.logContext();
        this.logEvent = new PnAuditLogBuilder()
                .before(logEventType, enrichedMsg)
                .build();
    }

    public static RaddAltAuditLogBuilder builder() {
        return new RaddAltAuditLogBuilder();
    }

    public PnRaddAltAuditLog log() {
        this.logEvent.log();
        return this;
    }

    public void generateFailure(String msg, Object... arguments) {
        this.logEvent.generateFailure(msg, arguments).log();
    }

    public void generateWarning(String msg, Object... arguments) {
        this.logEvent.generateWarning(msg, arguments).log();
    }

    public void generateSuccessWithContext(String msgPrefix) {
        this.logEvent.generateSuccess(msgPrefix + context.logContext()).log();
    }

    public void generateSuccess() {
        this.logEvent.generateSuccess().log();
    }

    public void generateSuccess(String msg, Object... arguments) {
        this.logEvent.generateSuccess(msg, arguments).log();
    }

    public static class RaddAltAuditLogBuilder {
        private String msg;
        private PnRaddAltLogContext context;
        private PnAuditLogEventType eventType;

        RaddAltAuditLogBuilder() {}

        public RaddAltAuditLogBuilder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public RaddAltAuditLogBuilder eventType(PnAuditLogEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public RaddAltAuditLogBuilder context(PnRaddAltLogContext context) {
            this.context = context;
            return this;
        }

        public PnRaddAltAuditLog build() {
            PnAuditLogEventType logEventType = getEventType();
            return new PnRaddAltAuditLog(logEventType, this.msg, context);
        }

        private PnAuditLogEventType getEventType() {
            if(eventType == null) {
                throw new RaddGenericException("Missing eventType in RaddAltAuditLogBuilder");
            }

            return eventType;
        }

    }
}
