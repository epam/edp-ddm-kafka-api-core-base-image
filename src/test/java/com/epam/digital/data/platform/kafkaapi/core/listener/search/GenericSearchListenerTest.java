package com.epam.digital.data.platform.kafkaapi.core.listener.search;

import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.listener.search.impl.GenericSearchListenerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntityContains;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.RequestContext;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.SecurityContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

import static com.epam.digital.data.platform.model.core.kafka.Status.INVALID_SIGNATURE;
import static com.epam.digital.data.platform.model.core.kafka.Status.JWT_INVALID;
import static com.epam.digital.data.platform.model.core.kafka.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = GenericSearchListenerTestImpl.class)
class GenericSearchListenerTest {

  @MockBean
  AbstractSearchHandler<MockEntityContains, MockEntity> searchHandler;
  @MockBean
  DigitalSignatureService digitalSignatureService;
  @MockBean
  JwtValidationService jwtValidationService;
  @MockBean
  ResponseMessageCreator responseMessageCreator;
  @Autowired
  GenericSearchListenerTestImpl instance;

  @Captor
  private ArgumentCaptor<Response<List<MockEntity>>> responseCaptor;

  @Test
  void shouldSearchInHandler() {
    when(digitalSignatureService.isSealValid(any(), any())).thenReturn(true);
    when(jwtValidationService.isValid(any())).thenReturn(true);
    var c = mockResult();

    given(searchHandler.search(any(Request.class))).willReturn(List.of(c));
    var mockResponse = new Response<>();
    mockResponse.setPayload(List.of(c));
    given(responseMessageCreator.createMessageByPayloadSize(any()))
            .willReturn(MessageBuilder.withPayload(mockResponse).build());

    var actualMessage = instance.search("datafactory-key", mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseCaptor.capture());
    var plainResponse= responseCaptor.getValue();
    assertThat(plainResponse.getStatus()).isEqualTo(SUCCESS);
    assertThat(plainResponse.getPayload()).hasSize(1);
    assertThat(plainResponse.getPayload().get(0)).isEqualTo(c);

    assertThat(actualMessage.getPayload()).isEqualTo(mockResponse);
  }

  @Test
  void shouldReturnInvalidSignatureStatus() {
    when(jwtValidationService.isValid(any())).thenReturn(true);
    when(digitalSignatureService.isSealValid(any(), any())).thenReturn(false);
    var mockResponse = new Response<>();
    when(responseMessageCreator.createMessageByPayloadSize(any()))
            .thenReturn(MessageBuilder.withPayload(mockResponse).build());

    var actualMessage = instance.search("datafactory-key", mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseCaptor.capture());
    var plainResponse = responseCaptor.getValue();
    assertThat(plainResponse.getStatus()).isEqualTo(INVALID_SIGNATURE);

    assertThat(actualMessage.getPayload()).isEqualTo(mockResponse);
  }

  @Test
  void shouldReturnInvalidJwtStatus() {
    when(jwtValidationService.isValid(any())).thenReturn(false);
    var mockResponse = new Response<>();
    when(responseMessageCreator.createMessageByPayloadSize(any()))
            .thenReturn(MessageBuilder.withPayload(mockResponse).build());

    var actualMessage = instance.search("datafactory-key", mockRequest());

    verify(responseMessageCreator).createMessageByPayloadSize(responseCaptor.capture());
    var plainResponse = responseCaptor.getValue();
    assertThat(plainResponse.getStatus()).isEqualTo(JWT_INVALID);

    assertThat(actualMessage.getPayload()).isEqualTo(mockResponse);
  }

  private MockEntityContains mockSc() {
    return new MockEntityContains();
  }

  private MockEntity mockResult() {
    var c = new MockEntity();
    c.setPersonFullName("Some Full Name");
    return c;
  }

  private Request<MockEntityContains> mockRequest() {
    return new Request<>(mockSc(), new RequestContext(), new SecurityContext());
  }
}
