package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.ResponseHeaders;
import com.epam.digital.data.platform.model.core.kafka.Status;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResponseMessageCreator {

  private static final String CEPH_MESSAGE_KEY_PREFIX = "datafactory-response-";

  private final Logger log = LoggerFactory.getLogger(ResponseMessageCreator.class);

  private final Integer messageSizeLimit;
  private final String cephBucketName;

  private final Serializer valueSerializer;
  private final CephService datafactoryResponseCephService;
  private final TraceProvider traceProvider;

  public ResponseMessageCreator(
      @Value("${data-platform.kafka.max-request-size}") Integer messageSizeLimit,
      @Value("${datafactory-response-ceph.bucket}") String cephBucketName,
      Serializer valueSerializer,
      CephService datafactoryResponseCephService,
      TraceProvider traceProvider) {
    this.messageSizeLimit = messageSizeLimit;
    this.valueSerializer = valueSerializer;
    this.cephBucketName = cephBucketName;
    this.datafactoryResponseCephService = datafactoryResponseCephService;
    this.traceProvider = traceProvider;
  }

  public <T> Message<Response<T>> createMessageByPayloadSize(Response<T> response) {
    Response<T> returnedToUserResponse;
    var serializedResponse = valueSerializer.serialize(null, response);
    if (serializedResponse != null && serializedResponse.length >= messageSizeLimit) {
      returnedToUserResponse = new Response<>();
      var cephContentKey = CEPH_MESSAGE_KEY_PREFIX + UUID.randomUUID();
      try {
        datafactoryResponseCephService.putContent(
            cephBucketName, cephContentKey, new String(serializedResponse, StandardCharsets.UTF_8));
        return MessageBuilder.withPayload(returnedToUserResponse)
                .setHeader(KafkaHeaders.MESSAGE_KEY, traceProvider.getRequestId())
                .setHeader(ResponseHeaders.CEPH_RESPONSE_KEY, cephContentKey)
                .build();
      } catch (CephCommunicationException e) {
        log.error("Exception while communication with ceph", e);
        returnedToUserResponse = new Response<>();
        returnedToUserResponse.setStatus(Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
      } catch (MisconfigurationException e) {
        log.error("Incorrect Ceph configuration", e);
        returnedToUserResponse = new Response<>();
        returnedToUserResponse.setStatus(Status.INTERNAL_CONTRACT_VIOLATION);
      }
    } else {
      returnedToUserResponse = response;
    }
    return MessageBuilder.withPayload(returnedToUserResponse)
            .setHeader(KafkaHeaders.MESSAGE_KEY, traceProvider.getRequestId())
            .build();
  }
}
