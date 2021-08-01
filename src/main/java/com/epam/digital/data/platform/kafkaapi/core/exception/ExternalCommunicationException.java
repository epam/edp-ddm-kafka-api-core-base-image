package com.epam.digital.data.platform.kafkaapi.core.exception;

import com.epam.digital.data.platform.model.core.kafka.Status;

public class ExternalCommunicationException extends RequestProcessingException {

  public ExternalCommunicationException(String message, Throwable cause,
      Status kafkaResponseStatus) {
    super(message, cause, kafkaResponseStatus);
  }

  public ExternalCommunicationException(String message, Status kafkaResponseStatus) {
    super(message, kafkaResponseStatus);
  }
}
