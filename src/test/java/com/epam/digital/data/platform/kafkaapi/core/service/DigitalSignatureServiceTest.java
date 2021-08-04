package com.epam.digital.data.platform.kafkaapi.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dso.api.dto.VerifyRequestDto;
import com.epam.digital.data.platform.dso.api.dto.VerifyResponseDto;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.dso.client.exception.BadRequestException;
import com.epam.digital.data.platform.dso.client.exception.InternalServerErrorException;
import com.epam.digital.data.platform.integration.ceph.exception.CephCommunicationException;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.kafkaapi.core.config.JooqTestConfig;
import com.epam.digital.data.platform.kafkaapi.core.exception.ExternalCommunicationException;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JooqTestConfig.class)
class DigitalSignatureServiceTest {

  private static final String BACKET = "backet";
  private static final String SIGNATURE = "signature";
  private static final String KEY = "datafactory-key";
  @Mock
  private CephService cephService;
  @Mock
  private DigitalSealRestClient digitalSealRestClient;
  @Autowired
  private ObjectMapper objectMapper;
  private DigitalSignatureService digitalSignatureService;
  private Request request;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    when(cephService.getContent(BACKET, KEY)).thenReturn(Optional.of(SIGNATURE));
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, objectMapper, true);
    request = new Request(getMockPayload(), null, null);
    when(digitalSealRestClient.verify(any()))
        .thenReturn(new VerifyResponseDto().toBuilder().build());
  }

  @Test
  void kafkaHeaderAbsentThrowsException() {
    assertThrows(ExternalCommunicationException.class,
        () -> digitalSignatureService.isSealValid(null, request));
  }

  @Test
  void kafkaHeaderAbsentReturnTrueWhenValidationDisabled() {
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, objectMapper, false);

    assertTrue(digitalSignatureService.isSealValid(null, request));
  }

  @Test
  void checkThatTheCorrectArgumentsArePassedToTheVerifyFunction() throws JsonProcessingException {
    digitalSignatureService.isSealValid(KEY, request);

    ArgumentCaptor<VerifyRequestDto> requestCaptor = ArgumentCaptor
        .forClass(VerifyRequestDto.class);
    verify(digitalSealRestClient).verify(requestCaptor.capture());

    assertEquals(SIGNATURE, requestCaptor.getValue().signature());
    assertEquals(objectMapper.writeValueAsString(request), requestCaptor.getValue().data());
  }

  @Test
  void CephCommunicationExceptionChangedToExternalCommunicationException() {
    when(cephService.getContent(any(), any())).thenThrow(CephCommunicationException.class);
    var actualException = assertThrows(ExternalCommunicationException.class,
        () -> digitalSignatureService.isSealValid(KEY, request));
    assertThat(actualException.getKafkaResponseStatus()).isEqualTo(Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
  }

  @Test
  void externalCommunicationExceptionWhenNotFoundCephContent() {
    when(cephService.getContent(any(), any())).thenReturn(Optional.empty());
    var actualException = assertThrows(ExternalCommunicationException.class,
            () -> digitalSignatureService.isSealValid(KEY, request));
    assertThat(actualException.getKafkaResponseStatus()).isEqualTo(Status.INTERNAL_CONTRACT_VIOLATION);
  }

  @Test
  void misconfigurationExceptionChangedToExternalCommunicationException() {
    when(cephService.getContent(any(), any())).thenThrow(MisconfigurationException.class);
    assertThrows(ExternalCommunicationException.class,
        () -> digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void badRequestExceptionChangedToExternalCommunicationException() {
    when(digitalSealRestClient.verify(any())).thenThrow(BadRequestException.class);
    assertThrows(ExternalCommunicationException.class,
        () -> digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void internalServerErrorExceptionChangedToExternalCommunicationException() {
    when(digitalSealRestClient.verify(any())).thenThrow(InternalServerErrorException.class);
    assertThrows(ExternalCommunicationException.class,
        () -> digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void CephCommunicationExceptionShoudlReturnTrueWhenValidationDisabled() {
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, objectMapper, false);
    when(cephService.getContent(any(), any())).thenThrow(CephCommunicationException.class);
    assertTrue(digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void misconfigurationExceptionShoudlReturnTrueWhenValidationDisabled() {
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, objectMapper, false);
    when(cephService.getContent(any(), any())).thenThrow(MisconfigurationException.class);
    assertTrue(digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void badRequestExceptionShoudlReturnTrueWhenValidationDisabled() {
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, objectMapper, false);
    when(digitalSealRestClient.verify(any())).thenThrow(BadRequestException.class);
    assertTrue(digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void internalServerErrorExceptionShoudlReturnTrueWhenValidationDisabled() {
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, objectMapper, false);
    when(digitalSealRestClient.verify(any())).thenThrow(InternalServerErrorException.class);
    assertTrue(digitalSignatureService.isSealValid(KEY, request));
  }

  @Test
  void JsonProcessingExceptionChangedToIllegalStateException() {
    digitalSignatureService = new DigitalSignatureService(cephService, BACKET,
        digitalSealRestClient, new ObjectMapper(), true);

    assertThrows(IllegalStateException.class,
        () -> digitalSignatureService.isSealValid(KEY, request));
  }

  private MockEntity getMockPayload() {
    MockEntity mockEntity = new MockEntity();
    mockEntity.setConsentId(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
    mockEntity.setConsentDate(LocalDateTime
        .parse("2021-02-06T12:33:20.111Z",
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
    mockEntity.setPersonFullName("Full name");
    mockEntity.setPersonPassNumber("АА123456");
    return mockEntity;
  }
}