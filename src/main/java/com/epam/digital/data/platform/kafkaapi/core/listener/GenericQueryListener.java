package com.epam.digital.data.platform.kafkaapi.core.listener;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.exception.RequestProcessingException;
import com.epam.digital.data.platform.kafkaapi.core.queryhandler.AbstractQueryHandler;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.model.core.kafka.EntityId;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

public abstract class GenericQueryListener<I, O> {

  @Autowired
  private DigitalSignatureService signatureService;
  @Autowired
  private JwtValidationService jwtValidationService;
  @Autowired
  private ResponseMessageCreator responseMessageCreator;

  public static final String DIGITAL_SEAL = "digital-seal";

  private final Logger log = LoggerFactory.getLogger(GenericQueryListener.class);
  private final AbstractCommandHandler<O> commandHandler;
  private final AbstractQueryHandler<I, O> queryHandler;

  protected GenericQueryListener(
      AbstractCommandHandler<O> commandHandler,
      AbstractQueryHandler<I, O> queryHandler) {
    this.commandHandler = commandHandler;
    this.queryHandler = queryHandler;
  }

  public Message<Response<EntityId>> create(String key, Request<O> input) {
    Response<EntityId> response = new Response<>();
    try {
      if (!isInputValid(key, input, response)) {
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      response.setPayload(commandHandler.save(input));
      response.setStatus(Status.CREATED);
    } catch (RequestProcessingException e) {
      log.error("Exception while request processing", e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    }
    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  public Message<Response<O>> read(String key, Request<I> input) {
    Response<O> response = new Response<>();
    try {
      if (!isInputValid(key, input, response)) {
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      Optional<O> consent = queryHandler.findById(input);
      if (consent.isPresent()) {
        response.setPayload(consent.get());
        response.setStatus(Status.SUCCESS);
      } else {
        response.setStatus(Status.NOT_FOUND);
      }
    } catch (RequestProcessingException e) {
      log.error("Exception while request processing", e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    }
    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  public Message<Response<Void>> update(String key, Request<O> input) {
    Response<Void> response = new Response<>();
    try {
      if (!isInputValid(key, input, response)) {
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      commandHandler.update(input);
      response.setStatus(Status.NO_CONTENT);
    } catch (RequestProcessingException e) {
      log.error("Exception while request processing", e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    }
    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  public Message<Response<Void>> delete(String key, Request<O> input) {
    Response<Void> response = new Response<>();
    try {
      if (!isInputValid(key, input, response)) {
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      commandHandler.delete(input);
      response.setStatus(Status.NO_CONTENT);
    } catch (RequestProcessingException e) {
      log.error("Exception while request processing", e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    }
    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  private <T, U> boolean isInputValid(String key, Request<T> input, Response<U> response) {
    if (!jwtValidationService.isValid(input)) {
      response.setStatus(Status.JWT_INVALID);
      return false;
    }
    if (!signatureService.isSealValid(key, input)) {
      response.setStatus(Status.INVALID_SIGNATURE);
      return false;
    }
    return true;
  }
}