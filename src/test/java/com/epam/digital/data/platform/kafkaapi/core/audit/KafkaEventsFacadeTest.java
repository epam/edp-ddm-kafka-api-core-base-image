package com.epam.digital.data.platform.kafkaapi.core.audit;

import static com.epam.digital.data.platform.kafkaapi.core.audit.AuditEventUtils.createSourceInfo;
import static com.epam.digital.data.platform.kafkaapi.core.audit.AuditEventUtils.createUserInfo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.service.TraceProvider;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.SecurityContext;
import com.epam.digital.data.platform.starter.audit.model.AuditEvent;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
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
  private static final String ACTION = "CREATE";
  private static final String STEP = "BEFORE";
  private static final LocalDateTime CURR_TIME = LocalDateTime.of(2021, 4, 1, 11, 50);
  private static final String RESULT = "RESULT";

  private static String ACCESS_TOKEN = "Token";

  private KafkaEventsFacade kafkaEventsFacade;
  private static JwtInfoProvider jwtInfoProvider;

  @Mock
  private AuditService auditService;
  @Mock
  private AuditSourceInfoProvider auditSourceInfoProvider;
  @Mock
  private TraceProvider traceProvider;

  private final Clock clock =
          Clock.fixed(CURR_TIME.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

  private AuditSourceInfo mockSourceInfo;

  @BeforeAll
  static void init() throws IOException {
    ACCESS_TOKEN = new String(ByteStreams.toByteArray(
        DatabaseEventsFacadeTest.class.getResourceAsStream("/accessToken.json")));
    jwtInfoProvider = new JwtInfoProvider(new TokenParser(new ObjectMapper()));
  }

  @BeforeEach
  void beforeEach() {
    kafkaEventsFacade =
        new KafkaEventsFacade(
            auditService, APP_NAME, clock, jwtInfoProvider, traceProvider, auditSourceInfoProvider);

    when(traceProvider.getRequestId()).thenReturn(REQUEST_ID);

    mockSourceInfo = createSourceInfo();
    when(auditSourceInfoProvider.getAuditSourceInfo())
            .thenReturn(mockSourceInfo);
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

    var expectedEvent = AuditEvent.AuditEventBuilder.anAuditEvent()
            .application(APP_NAME)
            .name("Kafka request. Method: method")
            .requestId(REQUEST_ID)
            .sourceInfo(mockSourceInfo)
            .userInfo(null)
            .currentTime(clock.millis())
            .eventType(EventType.USER_ACTION)
            .context(context)
            .build();

    assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);
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

    var expectedEvent = AuditEvent.AuditEventBuilder.anAuditEvent()
            .application(APP_NAME)
            .name("Kafka request. Method: method")
            .requestId(REQUEST_ID)
            .sourceInfo(mockSourceInfo)
            .userInfo(createUserInfo())
            .currentTime(clock.millis())
            .eventType(EventType.USER_ACTION)
            .context(context)
            .build();

    assertThat(actualEvent).usingRecursiveComparison().isEqualTo(expectedEvent);
  }

  private Request mockRequest(String jwt) {
    SecurityContext sc = new SecurityContext(jwt, null, null);
    return new Request(null, null, sc);
  }
}