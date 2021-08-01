package com.epam.digital.data.platform.kafkaapi.core.exception;

import com.epam.digital.data.platform.model.core.kafka.Status;

public class RequestProcessingException extends RuntimeException {

  private final Status kafkaResponseStatus;
  private String details;

  public RequestProcessingException(String message, Status kafkaResponseStatus) {
    super(message);
    this.kafkaResponseStatus = kafkaResponseStatus;
  }

  public RequestProcessingException(String message, Throwable cause, Status kafkaResponseStatus) {
    super(message, cause);
    this.kafkaResponseStatus = kafkaResponseStatus;
  }

  public RequestProcessingException(String message, Status kafkaResponseStatus, String details) {
    super(message);
    this.kafkaResponseStatus = kafkaResponseStatus;
    this.details = details;
  }

  public RequestProcessingException(String message, Throwable cause, Status kafkaResponseStatus, String details) {
    super(message, cause);
    this.kafkaResponseStatus = kafkaResponseStatus;
    this.details = details;
  }

  public Status getKafkaResponseStatus() {
    return kafkaResponseStatus;
  }

  public String getDetails() {
    return details;
  }
}
