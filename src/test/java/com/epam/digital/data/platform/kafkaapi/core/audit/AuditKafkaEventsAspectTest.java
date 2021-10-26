package com.epam.digital.data.platform.kafkaapi.core.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.listener.impl.GenericQueryListenerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.queryhandler.AbstractQueryHandler;
import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

@Import({AopAutoConfiguration.class})
@SpringBootTest(
    classes = {
        AuditAspect.class,
        KafkaAuditProcessor.class,
        GenericQueryListenerTestImpl.class
    })
@MockBean(DatabaseAuditProcessor.class)
@MockBean(AbstractSearchHandler.class)
@MockBean(AbstractCommandHandler.class)
@MockBean(AbstractQueryHandler.class)
class AuditKafkaEventsAspectTest {

  @Autowired
  private GenericQueryListenerTestImpl genericQueryListener;

  @MockBean
  private KafkaEventsFacade kafkaEventsFacade;
  @MockBean
  private DigitalSignatureService signatureService;
  @MockBean
  private JwtValidationService jwtValidationService;
  @MockBean
  private ResponseMessageCreator responseMessageCreator;

  @BeforeEach
  void beforeEach() {
    when(responseMessageCreator.createMessageByPayloadSize(any()))
        .thenReturn(MessageBuilder.withPayload(new Response<>()).build());
  }

  @Test
  void expectAuditAspectBeforeAndAfterCreateMethodWhenNoException() {

    genericQueryListener.create("", new Request());

    verify(kafkaEventsFacade, times(2))
        .sendKafkaAudit(any(), any(), any(), any(), any(), any());
  }

  @Test
  void expectAuditAspectBeforeAndAfterCreateMethodWhenAnyException() {
    when(jwtValidationService.isValid(any())).thenThrow(new RuntimeException());

    genericQueryListener.create("", new Request());

    verify(kafkaEventsFacade, times(2))
        .sendKafkaAudit(any(), any(), any(), any(), any(), any());
  }

  @Test
  void expectAuditAspectBeforeAndAfterUpdateMethodWhenNoException() {

    genericQueryListener.update("", new Request());

    verify(kafkaEventsFacade, times(2))
        .sendKafkaAudit(any(), any(), any(), any(), any(), any());
  }

  @Test
  void expectAuditAspectBeforeAndAfterUpdateMethodWhenAnyException() {
    when(jwtValidationService.isValid(any())).thenThrow(new RuntimeException());

    genericQueryListener.update("", new Request());

    verify(kafkaEventsFacade, times(2))
        .sendKafkaAudit(any(), any(), any(), any(), any(), any());
  }

  @Test
  void expectAuditAspectBeforeAndAfterDeleteMethodWhenNoException() {

    genericQueryListener.delete("", new Request());

    verify(kafkaEventsFacade, times(2))
        .sendKafkaAudit(any(), any(), any(), any(), any(), any());
  }

  @Test
  void expectAuditAspectBeforeAndAfterDeleteMethodWhenAnyException() {
    when(jwtValidationService.isValid(any())).thenThrow(new RuntimeException());

    genericQueryListener.delete("", new Request());

    verify(kafkaEventsFacade, times(2))
        .sendKafkaAudit(any(), any(), any(), any(), any(), any());
  }
}
