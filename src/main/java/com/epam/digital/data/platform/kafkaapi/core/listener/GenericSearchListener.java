package com.epam.digital.data.platform.kafkaapi.core.listener;

import com.epam.digital.data.platform.kafkaapi.core.exception.RequestProcessingException;
import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

public abstract class GenericSearchListener<I, O> {

  protected static final String DIGITAL_SEAL = GenericQueryListener.DIGITAL_SEAL;
  private static final String INPUT_IS_INVALID = "Input is invalid";

  private final Logger log = LoggerFactory.getLogger(GenericSearchListener.class);

  @Autowired
  private DigitalSignatureService signatureService;
  @Autowired
  private JwtValidationService jwtValidationService;
  @Autowired
  private ResponseMessageCreator responseMessageCreator;

  private final AbstractSearchHandler<I, O> searchHandler;

  protected GenericSearchListener(AbstractSearchHandler<I, O> searchHandler) {
    this.searchHandler = searchHandler;
  }

  public Message<Response<List<O>>> search(String key, Request<I> input) {
    Response<List<O>> response = new Response<>();

    try {
      if (!isInputValid(key, input, response)) {
        log.info(INPUT_IS_INVALID);
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      List<O> found = searchHandler.search(input);
      response.setPayload(found);
      response.setStatus(Status.SUCCESS);
    } catch (RequestProcessingException e) {
      log.error("Exception while request processing", e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    } catch (Exception e) {
      log.error("Unexpected exception while executing the 'delete' method", e);
      response.setStatus(Status.OPERATION_FAILED);
      response.setDetails("Unexpected exception while executing the 'delete' method");
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
