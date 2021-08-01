package com.epam.digital.data.platform.kafkaapi.core.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.starter.audit.model.AuditEvent;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
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
class DatabaseEventsFacadeTest {

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
  private static JwtClaimsDto userClaims;

  private static TokenParser tokenParser;
  private static String ACCESS_TOKEN;

  private DatabaseEventsFacade databaseEventsFacade;

  @Mock
  private AuditService auditService;
  @Mock
  private TraceProvider traceProvider;
  @Mock
  private Clock clock;

  @BeforeAll
  static void init() throws IOException {
    tokenParser = new TokenParser(new ObjectMapper());
    ACCESS_TOKEN = new String(ByteStreams.toByteArray(
        DatabaseEventsFacadeTest.class.getResourceAsStream("/accessToken.json")));
    userClaims = tokenParser.parseClaims(ACCESS_TOKEN);
  }

  @BeforeEach
  void beforeEach() {
    databaseEventsFacade = new DatabaseEventsFacade(APP_NAME, auditService, traceProvider, clock);

    when(traceProvider.getRequestId()).thenReturn(REQUEST_ID);
    when(clock.millis())
        .thenReturn(CURR_TIME.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
  }

  @Test
  void expectCorrectAuditEvent() {
    Map<String, Object> context = Map.of(
        "action", ACTION,
        "step", STEP,
        "tablename", TABLE_NAME,
        "row_id", "42",
        "fields", FIELDS,
        "result", RESULT
    );
    when(auditService.createContext(ACTION, STEP, TABLE_NAME, "42", FIELDS, RESULT))
        .thenReturn(context);

    databaseEventsFacade
        .sendDbAudit(METHOD_NAME, TABLE_NAME, ACTION, userClaims, STEP, "42", FIELDS, RESULT);

    ArgumentCaptor<AuditEvent> auditEventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

    verify(auditService).sendAudit(auditEventCaptor.capture());
    AuditEvent actualEvent = auditEventCaptor.getValue();

    assertThat(actualEvent.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(actualEvent.getApplication()).isEqualTo(APP_NAME);
    assertThat(actualEvent.getEventType()).isEqualTo(EventType.USER_ACTION);
    assertThat(actualEvent.getCurrentTime()).isEqualTo(clock.millis());
    assertThat(actualEvent.getUserId()).isEqualTo(USER_ID);
    assertThat(actualEvent.getUserName()).isEqualTo(USER_NAME);
    assertThat(actualEvent.getName()).isEqualTo("DB request. Method: method");
    assertThat(actualEvent.getContext()).isEqualTo(context);
  }
}
