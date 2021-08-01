package com.epam.digital.data.platform.kafkaapi.core.listener;

import com.epam.digital.data.platform.kafkaapi.core.exception.RequestProcessingException;
import com.epam.digital.data.platform.kafkaapi.core.queryhandler.AbstractQueryHandler;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

public abstract class GenericRoleBasedQueryListener<I, O> {

  public static final String DIGITAL_SEAL = "digital-seal";

  @Autowired
  private DigitalSignatureService signatureService;
  @Autowired
  private JwtValidationService jwtValidationService;
  @Autowired
  private ResponseMessageCreator responseMessageCreator;

  private final Logger log = LoggerFactory.getLogger(GenericRoleBasedQueryListener.class);
  private final AbstractQueryHandler<I, O> queryHandler;

  protected GenericRoleBasedQueryListener(
      AbstractQueryHandler<I, O> queryHandler) {
    this.queryHandler = queryHandler;
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

  private boolean isInputValid(String key, Request<I> input, Response<O> response) {
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
