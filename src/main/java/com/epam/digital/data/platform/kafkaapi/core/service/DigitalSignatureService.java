package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.dso.api.dto.VerifyRequestDto;
import com.epam.digital.data.platform.dso.api.dto.VerifyResponseDto;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.dso.client.exception.BadRequestException;
import com.epam.digital.data.platform.dso.client.exception.InternalServerErrorException;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.kafkaapi.core.exception.ExternalCommunicationException;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class DigitalSignatureService {

  private final CephService datafactoryCephService;
  private final String cephBucketName;
  private final DigitalSealRestClient digitalSealRestClient;
  private final ObjectMapper objectMapper;

  private final boolean isEnabled;

  public DigitalSignatureService(
      CephService datafactoryCephService,
      @Value("${ceph.bucket}") String cephBucketName,
      DigitalSealRestClient digitalSealRestClient,
      ObjectMapper objectMapper,
      @Value("${data-platform.kafka-request.validation.enabled}") boolean isEnabled) {
    this.datafactoryCephService = datafactoryCephService;
    this.cephBucketName = cephBucketName;
    this.digitalSealRestClient = digitalSealRestClient;
    this.objectMapper = objectMapper;
    this.isEnabled = isEnabled;
  }

  public <O> boolean isSealValid(String key, O input) {
    if (!isEnabled) {
      return true;
    }

    if (key == null) {
      throw new ExternalCommunicationException(
          "Required kafka header is missing",
          new MessageHandlingException(new GenericMessage<>("Required kafka header is missing")),
          Status.INTERNAL_CONTRACT_VIOLATION);
    }

    String signature;
    try {
      signature =
          datafactoryCephService
              .getContent(cephBucketName, key)
              .orElseThrow(
                  () ->
                      new ExternalCommunicationException(
                          "Digital signature does not found in ceph",
                          Status.INTERNAL_CONTRACT_VIOLATION));
    } catch (CephCommunicationException e) {
      throw new ExternalCommunicationException(
          "Exception while communication with ceph", e, Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
    } catch (MisconfigurationException e) {
      throw new ExternalCommunicationException(
          "Incorrect Ceph configuration", e, Status.INTERNAL_CONTRACT_VIOLATION);
    }

    return verify(signature, serialize(input));
  }

  private boolean verify(String signature, String data) {
    try {
      VerifyResponseDto responseDto =
          digitalSealRestClient.verify(new VerifyRequestDto(signature, data));
      return responseDto.isValid;
    } catch (BadRequestException e) {
      throw new ExternalCommunicationException(
          "Call to external digital signature service violates an internal contract",
          e,
          Status.INTERNAL_CONTRACT_VIOLATION);
    } catch (InternalServerErrorException e) {
      throw new ExternalCommunicationException(
          "External digital signature service has internal server error",
          e,
          Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
    }
  }

  private <T> String serialize(T object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Couldn't serialize object", e);
    }
  }
}