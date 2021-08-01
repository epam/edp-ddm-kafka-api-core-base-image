package com.epam.digital.data.platform.kafkaapi.core.exception;

import com.epam.digital.data.platform.model.core.kafka.Status;

public class JwtVerificationException extends RequestProcessingException {
    public JwtVerificationException(String message, Throwable cause) {
        super(message, cause, Status.JWT_INVALID);
    }
}
