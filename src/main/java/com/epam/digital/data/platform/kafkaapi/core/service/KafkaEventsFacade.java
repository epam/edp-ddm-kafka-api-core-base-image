package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventsFacade extends AbstractAuditFacade {

  static final String KAFKA_REQUEST = "Kafka request. Method: ";

  private final JwtInfoProvider jwtInfoProvider;
  private final TraceProvider traceProvider;

  public KafkaEventsFacade(
      @Value("${spring.application.name:kafka-api}") String appName,
      AuditService auditService,
      Clock clock, JwtInfoProvider jwtInfoProvider,
      TraceProvider traceProvider) {
    super(appName, auditService, clock);
    this.jwtInfoProvider = jwtInfoProvider;
    this.traceProvider = traceProvider;
  }

  public void sendKafkaAudit(EventType eventType, String methodName, Request<?> request,
      String action, String step, String result) {
    var event = createBaseAuditEvent(eventType, KAFKA_REQUEST + methodName,
        traceProvider.getRequestId())
        .setBusinessProcessInfo(traceProvider.getSourceSystem(),
            traceProvider.getSourceBusinessId(), traceProvider.getSourceBusinessProcess());

    var context = auditService.createContext(action, step, null, null, null, result);
    event.setContext(context);

    setUserInfo(request, event);

    auditService.sendAudit(event.build());
  }

  private void setUserInfo(Request<?> request, GroupedAuditEventBuilder event) {
    String jwt = request.getSecurityContext().getAccessToken();
    if (jwt != null) {
      var userClaims = jwtInfoProvider.getUserClaims(request);
      event.setUserInfo(userClaims.getDrfo(), userClaims.getFullName());
    }
  }
}
