package com.epam.digital.data.platform.kafkaapi.core.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.SecurityContext;
import com.epam.digital.data.platform.starter.audit.model.AuditEvent;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaEventsFacadeTest {

  private static final String APP_NAME = "application";
  private static final String REQUEST_ID = "1";
  private static final String METHOD_NAME = "method";
  private static final String TABLE_NAME = "table";
  private static final String ACTION = "CREATE";
  private static final String STEP = "BEFORE";
  private static final String USER_ID = "1010101014";
  private static final String USER_NAME = "Сидоренко Василь Леонідович";
  private static final LocalDateTime CURR_TIME = LocalDateTime.of(2021, 4, 1, 11, 50);
  private static final Set<String> FIELDS = Set.of("first", "second");
  private static final String RESULT = "RESULT";

  private static String ACCESS_TOKEN = "Token";

  private KafkaEventsFacade kafkaEventsFacade;
  private static JwtInfoProvider jwtInfoProvider;


  @Mock
  private AuditService auditService;
  @Mock
  private TraceProvider traceProvider;
  @Mock
  private Clock clock;

  @BeforeAll
  static void init() throws IOException {
    ACCESS_TOKEN = new String(ByteStreams.toByteArray(
        DatabaseEventsFacadeTest.class.getResourceAsStream("/accessToken.json")));
    jwtInfoProvider = new JwtInfoProvider(new TokenParser(new ObjectMapper()));
  }

  @BeforeEach
  void beforeEach() {
    kafkaEventsFacade = new KafkaEventsFacade(
        APP_NAME, auditService, clock, jwtInfoProvider, traceProvider);

    when(traceProvider.getRequestId()).thenReturn(REQUEST_ID);
    when(clock.millis())
        .thenReturn(CURR_TIME.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
  }

  @Test
  void expectCorrectAuditEventWhenJwtAbsent() {
    Map<String, Object> context = Map.of("action", ACTION, "step", STEP, "result", RESULT);
    when(auditService.createContext(ACTION, STEP, null, null, null, RESULT))
        .thenReturn(context);

    kafkaEventsFacade.sendKafkaAudit(
        EventType.USER_ACTION, METHOD_NAME, mockRequest(null), ACTION, STEP, RESULT);

    ArgumentCaptor<AuditEvent> auditEventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

    verify(auditService).sendAudit(auditEventCaptor.capture());
    AuditEvent actualEvent = auditEventCaptor.getValue();

    assertThat(actualEvent.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(actualEvent.getApplication()).isEqualTo(APP_NAME);
    assertThat(actualEvent.getEventType()).isEqualTo(EventType.USER_ACTION);
    assertThat(actualEvent.getCurrentTime()).isEqualTo(clock.millis());
    assertThat(actualEvent.getUserId()).isNull();
    assertThat(actualEvent.getUserName()).isNull();
    assertThat(actualEvent.getName()).isEqualTo("Kafka request. Method: method");
    assertThat(actualEvent.getContext()).isEqualTo(context);
  }

  @Test
  void expectCorrectAuditEventWhenJwtPresent() {
    Map<String, Object> context = Map.of("action", ACTION, "step", STEP, "result", RESULT);
    when(auditService.createContext(ACTION, STEP, null, null, null, RESULT))
        .thenReturn(context);

    kafkaEventsFacade.sendKafkaAudit(
        EventType.USER_ACTION, METHOD_NAME, mockRequest(ACCESS_TOKEN), ACTION, STEP, RESULT);

    ArgumentCaptor<AuditEvent> auditEventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

    verify(auditService).sendAudit(auditEventCaptor.capture());
    AuditEvent actualEvent = auditEventCaptor.getValue();

    assertThat(actualEvent.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(actualEvent.getApplication()).isEqualTo(APP_NAME);
    assertThat(actualEvent.getEventType()).isEqualTo(EventType.USER_ACTION);
    assertThat(actualEvent.getCurrentTime()).isEqualTo(clock.millis());
    assertThat(actualEvent.getUserId()).isEqualTo(USER_ID);
    assertThat(actualEvent.getUserName()).isEqualTo(USER_NAME);
    assertThat(actualEvent.getName()).isEqualTo("Kafka request. Method: method");
    assertThat(actualEvent.getContext()).isEqualTo(context);
  }

  private Request mockRequest(String jwt) {
    SecurityContext sc = new SecurityContext(jwt, null, null);
    return new Request(null, null, sc);
  }
}