package com.epam.digital.data.platform.kafkaapi.core.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.impl.CommandHandlerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.exception.ExternalCommunicationException;
import com.epam.digital.data.platform.kafkaapi.core.exception.JwtValidationException;
import com.epam.digital.data.platform.kafkaapi.core.exception.ProcedureErrorException;
import com.epam.digital.data.platform.kafkaapi.core.listener.impl.GenericQueryListenerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.queryhandler.impl.QueryHandlerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootTest(classes = GenericQueryListenerTestImpl.class)
class GenericQueryListenerTest {

  private static final UUID ENTITY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
  private static final String KEY = "datafactory-key";

  @MockBean
  QueryHandlerTestImpl mockQueryHandler;
  @MockBean
  CommandHandlerTestImpl mockCommandHandler;
  @MockBean
  DigitalSignatureService digitalSignatureService;
  @MockBean
  JwtValidationService jwtValidationService;
  @MockBean
  ResponseMessageCreator responseMessageCreator;
  @Autowired
  private GenericQueryListenerTestImpl queryListener;

  @Captor
  private ArgumentCaptor<Response<MockEntity>> responseWithPayloadCaptor;

  @Captor
  private ArgumentCaptor<Response<Void>> responseNoPayloadCaptor;

  @BeforeEach
  void init() {
    when(digitalSignatureService.isSealValid(any(), any())).thenReturn(true);
    when(jwtValidationService.isValid(any())).thenReturn(true);

    when(responseMessageCreator.createMessageByPayloadSize(any()))
            .thenReturn(MessageBuilder.withPayload(new Response<>()).build());
  }

  @Test
  @DisplayName("Check if status is correct if entity saved")
  void happyWritePath() {
    queryListener.create(KEY, mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseNoPayloadCaptor.capture());
    var response = responseNoPayloadCaptor.getValue();
    assertThat(response.getStatus()).isEqualTo(Status.CREATED);
  }

  @Test
  @DisplayName("Check if response is valid when DB error")
  void procedureErrorOnWrite() {
    doThrow(new ProcedureErrorException("")).when(mockCommandHandler).save(any());

    queryListener.create("", mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseNoPayloadCaptor.capture());
    var response = responseNoPayloadCaptor.getValue();
    assertThat(response.getStatus()).isEqualTo(Status.PROCEDURE_ERROR);
  }

  @Test
  @DisplayName("Check if status is correct if entity updated")
  void happyUpdatePath() {
    queryListener.update(KEY, mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseNoPayloadCaptor.capture());
    var response = responseNoPayloadCaptor.getValue();
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT);
  }

  @Test
  @DisplayName("Check if status is correct if entity deleted")
  void happyDeletePath() {
    queryListener.delete(KEY, mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseNoPayloadCaptor.capture());
    var response = responseNoPayloadCaptor.getValue();
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT);
  }

  @Test
  @DisplayName("Check response when JWT token is invalid")
  void jwtTokenNotVerified() {
    when(jwtValidationService.isValid(any())).thenThrow(new JwtValidationException(""));

    queryListener.create(KEY, mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseNoPayloadCaptor.capture());
    var response = responseNoPayloadCaptor.getValue();
    assertThat(response.getStatus()).isEqualTo(Status.JWT_INVALID);
  }

  @Test
  @DisplayName("Check if response is valid when external service is unavailable")
  void externalSignatureServiceIsUnavailable() {
    when(digitalSignatureService.isSealValid(any(), any()))
        .thenThrow(
            new ExternalCommunicationException(
                "", new RuntimeException(), Status.THIRD_PARTY_SERVICE_UNAVAILABLE));

    queryListener.create(KEY, mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseNoPayloadCaptor.capture());
    var response = responseNoPayloadCaptor.getValue();
    assertThat(response.getStatus()).isEqualTo(Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
  }

  private Request<UUID> mockInput() {
    return new Request<>(ENTITY_ID, null, null);
  }

  private Request<MockEntity> mockRequest() {
    MockEntity mock = new MockEntity();
    Request<MockEntity> request = new Request<>();

    mock.setConsentId(ENTITY_ID);
    mock.setPersonFullName("stub");
    request.setPayload(mock);

    return request;
  }
}
