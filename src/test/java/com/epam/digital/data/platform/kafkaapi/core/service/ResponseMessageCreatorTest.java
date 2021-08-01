package com.epam.digital.data.platform.kafkaapi.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.ceph.exception.CephCommuncationException;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.ResponseHeaders;
import com.epam.digital.data.platform.model.core.kafka.Status;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;

@ExtendWith(MockitoExtension.class)
class ResponseMessageCreatorTest {

  private static final String REQUEST_ID = "1";
  private static final Integer MAX_ALLOWED_MESSAGE_SIZE = 5;
  private static final String BUCKET_NAME = "name";

  private ResponseMessageCreator responseMessageCreator;

  @Mock
  private Serializer<Response<MockEntity>> valueSerializer;
  @Mock
  private TraceProvider traceProvider;
  @Mock
  private CephService cephService;

  @BeforeEach
  void beforeEach() {
    responseMessageCreator =
        new ResponseMessageCreator(
            MAX_ALLOWED_MESSAGE_SIZE, BUCKET_NAME, valueSerializer, cephService, traceProvider);

    when(traceProvider.getRequestId()).thenReturn(REQUEST_ID);
  }

  @Test
  void expectNoResponseChangesIfSizeIsSmallerThanMax() {
    var responseToProcess = mockResponse();
    var serializedResponseStr = "qwer";
    when(valueSerializer.serialize(null, responseToProcess)).thenReturn(serializedResponseStr.getBytes());

    var actualResponseMessage =
        responseMessageCreator.createMessageByPayloadSize(responseToProcess);

    assertThat(actualResponseMessage.getHeaders().get(KafkaHeaders.MESSAGE_KEY)).isEqualTo(REQUEST_ID);
    assertThat(actualResponseMessage.getPayload()).isEqualTo(responseToProcess);
  }

  @Test
  void expectSaveToCephIfSizeIsLargerThanMax() {
    var responseToProcess = mockResponse();
    var serializedResponseStr = "qwerty";
    when(valueSerializer.serialize(null, responseToProcess)).thenReturn(serializedResponseStr.getBytes());

    var actualResponseMessage =
            responseMessageCreator.createMessageByPayloadSize(responseToProcess);

    verify(cephService).putContent(eq(BUCKET_NAME), any(), eq(serializedResponseStr));

    assertThat(actualResponseMessage.getHeaders().get(KafkaHeaders.MESSAGE_KEY)).isEqualTo(REQUEST_ID);
    assertThat(actualResponseMessage.getHeaders().get(ResponseHeaders.CEPH_RESPONSE_KEY)).isNotNull();
    var actualResponsePayload = actualResponseMessage.getPayload();
    assertThat(actualResponsePayload.getPayload()).isNull();
    assertThat(actualResponsePayload.getStatus()).isNull();
    assertThat(actualResponsePayload.getDetails()).isNull();
  }

  @Test
  void expectErrorStatusWhenCephCommunicationException() {
    var responseToProcess = mockResponse();
    var serializedResponseStr = "qwerty";
    when(valueSerializer.serialize(null, responseToProcess)).thenReturn(serializedResponseStr.getBytes());
    doThrow(new CephCommuncationException("", new RuntimeException()))
        .when(cephService)
        .putContent(any(), any(), any());

    var actualResponseMessage =
            responseMessageCreator.createMessageByPayloadSize(responseToProcess);

    assertThat(actualResponseMessage.getHeaders().get(KafkaHeaders.MESSAGE_KEY)).isEqualTo(REQUEST_ID);
    assertThat(actualResponseMessage.getHeaders().get(ResponseHeaders.CEPH_RESPONSE_KEY)).isNull();
    var actualResponsePayload = actualResponseMessage.getPayload();
    assertThat(actualResponsePayload.getPayload()).isNull();
    assertThat(actualResponsePayload.getStatus()).isEqualTo(Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
    assertThat(actualResponsePayload.getDetails()).isNull();
  }

  @Test
  void expectErrorStatusWhenMisconfigurationException() {
    var responseToProcess = mockResponse();
    var serializedResponseStr = "qwerty";
    when(valueSerializer.serialize(null, responseToProcess)).thenReturn(serializedResponseStr.getBytes());
    doThrow(new MisconfigurationException(""))
            .when(cephService)
            .putContent(any(), any(), any());

    var actualResponseMessage =
            responseMessageCreator.createMessageByPayloadSize(responseToProcess);

    assertThat(actualResponseMessage.getHeaders().get(KafkaHeaders.MESSAGE_KEY)).isEqualTo(REQUEST_ID);
    assertThat(actualResponseMessage.getHeaders().get(ResponseHeaders.CEPH_RESPONSE_KEY)).isNull();
    var actualResponsePayload = actualResponseMessage.getPayload();
    assertThat(actualResponsePayload.getPayload()).isNull();
    assertThat(actualResponsePayload.getStatus()).isEqualTo(Status.INTERNAL_CONTRACT_VIOLATION);
    assertThat(actualResponsePayload.getDetails()).isNull();
  }

  private Response<MockEntity> mockResponse() {
    var response = new Response<MockEntity>();
    response.setPayload(mockPayload());
    response.setStatus(Status.SUCCESS);
    return response;
  }

  private MockEntity mockPayload() {
    var c = new MockEntity();
    c.setPersonFullName("Some Full Name");
    return c;
  }
}
