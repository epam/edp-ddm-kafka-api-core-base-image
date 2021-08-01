package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import java.time.Clock;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEventsFacade extends AbstractAuditFacade {

  static final String DB_MODIFYING = "DB request. Method: ";

  private final TraceProvider traceProvider;

  public DatabaseEventsFacade(
      @Value("${spring.application.name:kafka-api}") String appName,
      AuditService auditService,
      TraceProvider traceProvider,
      Clock clock) {
    super(appName, auditService, clock);
    this.traceProvider = traceProvider;
  }

  public void sendDbAudit(String methodName, String tableName, String action,
      JwtClaimsDto userClaims, String step, String entityId, Set<String> fields, String result) {
    var event = createBaseAuditEvent(EventType.USER_ACTION, DB_MODIFYING + methodName,
        traceProvider.getRequestId())
        .setBusinessProcessInfo(traceProvider.getSourceSystem(),
            traceProvider.getSourceBusinessId(), traceProvider.getSourceBusinessProcess());

    var context = auditService.createContext(action, step, tableName, entityId, fields, result);
    event.setContext(context);

    event.setUserInfo(userClaims.getDrfo(), userClaims.getFullName());

    auditService.sendAudit(event.build());
  }
}
