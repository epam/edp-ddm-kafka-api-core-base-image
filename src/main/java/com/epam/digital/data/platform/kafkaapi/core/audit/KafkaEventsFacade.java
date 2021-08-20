package com.epam.digital.data.platform.kafkaapi.core.audit;

import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.service.TraceProvider;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.audit.model.AuditUserInfo;
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
  private final AuditSourceInfoProvider auditSourceInfoProvider;

  public KafkaEventsFacade(
      AuditService auditService,
      @Value("${spring.application.name:kafka-api}") String appName,
      Clock clock,
      JwtInfoProvider jwtInfoProvider,
      TraceProvider traceProvider,
      AuditSourceInfoProvider auditSourceInfoProvider) {
    super(auditService, appName, clock);
    this.jwtInfoProvider = jwtInfoProvider;
    this.traceProvider = traceProvider;
    this.auditSourceInfoProvider = auditSourceInfoProvider;
  }

  public void sendKafkaAudit(EventType eventType, String methodName, Request<?> request,
                             String action, String step, String result) {
    var event =
        createBaseAuditEvent(eventType, KAFKA_REQUEST + methodName, traceProvider.getRequestId())
            .setSourceInfo(auditSourceInfoProvider.getAuditSourceInfo());

    var context = auditService.createContext(action, step, null, null, null, result);
    event.setContext(context);

    setUserInfo(request, event);

    auditService.sendAudit(event.build());
  }

  private void setUserInfo(Request<?> request, GroupedAuditEventBuilder event) {
    String jwt = request.getSecurityContext().getAccessToken();
    if (jwt != null) {
      var userClaims = jwtInfoProvider.getUserClaims(request);
      var userInfo = AuditUserInfo.AuditUserInfoBuilder.anAuditUserInfo()
              .userName(userClaims.getFullName())
              .userKeycloakId(userClaims.getSubject())
              .userDrfo(userClaims.getDrfo())
              .build();
      event.setUserInfo(userInfo);
    }
  }
}