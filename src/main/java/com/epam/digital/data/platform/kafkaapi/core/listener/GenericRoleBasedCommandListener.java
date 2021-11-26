/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.kafkaapi.core.listener;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.exception.RequestProcessingException;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.model.core.kafka.EntityId;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

public abstract class GenericRoleBasedCommandListener<I, O> {

  protected static final String DIGITAL_SEAL = "digital-seal";

  private static final String INPUT_IS_INVALID = "Input is invalid";
  private static final String EXCEPTION_WHILE_REQUEST_PROCESSING = "Exception while request processing";

  private final Logger log = LoggerFactory.getLogger(GenericRoleBasedCommandListener.class);

  @Autowired
  private DigitalSignatureService signatureService;
  @Autowired
  private JwtValidationService jwtValidationService;
  @Autowired
  private ResponseMessageCreator responseMessageCreator;

  private final AbstractCommandHandler<O> commandHandler;

  protected GenericRoleBasedCommandListener(
      AbstractCommandHandler<O> commandHandler) {
    this.commandHandler = commandHandler;
  }

  public Message<Response<EntityId>> create(String key, Request<O> input) {
    Response<EntityId> response = new Response<>();

    try {
      if (!isInputValid(key, input, response)) {
        log.info(INPUT_IS_INVALID);
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      response.setPayload(commandHandler.save(input));
      response.setStatus(Status.CREATED);
    } catch (RequestProcessingException e) {
      log.error(EXCEPTION_WHILE_REQUEST_PROCESSING, e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    } catch (Exception e) {
      log.error("Unexpected exception while executing the 'create' method", e);
      response.setStatus(Status.OPERATION_FAILED);
      response.setDetails("Unexpected exception while executing the 'create' method");
    }

    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  public Message<Response<Void>> update(String key, Request<O> input) {
    Response<Void> response = new Response<>();

    try {
      if (!isInputValid(key, input, response)) {
        log.info(INPUT_IS_INVALID);
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      commandHandler.update(input);
      response.setStatus(Status.NO_CONTENT);
    } catch (RequestProcessingException e) {
      log.error(EXCEPTION_WHILE_REQUEST_PROCESSING, e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    } catch (Exception e) {
      log.error("Unexpected exception while executing the 'update' method", e);
      response.setStatus(Status.OPERATION_FAILED);
      response.setDetails("Unexpected exception while executing the 'update' method");
    }

    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  public Message<Response<Void>> delete(String key, Request<O> input) {
    Response<Void> response = new Response<>();

    try {
      if (!isInputValid(key, input, response)) {
        log.info(INPUT_IS_INVALID);
        return responseMessageCreator.createMessageByPayloadSize(response);
      }

      commandHandler.delete(input);
      response.setStatus(Status.NO_CONTENT);
    } catch (RequestProcessingException e) {
      log.error(EXCEPTION_WHILE_REQUEST_PROCESSING, e);
      response.setStatus(e.getKafkaResponseStatus());
      response.setDetails(e.getDetails());
    } catch (Exception e) {
      log.error("Unexpected exception while executing the 'delete' method", e);
      response.setStatus(Status.OPERATION_FAILED);
      response.setDetails("Unexpected exception while executing the 'delete' method");
    }

    return responseMessageCreator.createMessageByPayloadSize(response);
  }

  private boolean isInputValid(String key, Request<O> input, Response<?> response) {
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
