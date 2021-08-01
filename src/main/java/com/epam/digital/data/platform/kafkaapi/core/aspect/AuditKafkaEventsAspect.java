package com.epam.digital.data.platform.kafkaapi.core.aspect;

import com.epam.digital.data.platform.kafkaapi.core.service.KafkaEventsFacade;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.epam.digital.data.platform.starter.audit.model.EventType;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditKafkaEventsAspect implements KafkaGenericListenerAspect {

  private final Set<Status> auditableStatusToHandler =
      Set.of(Status.INVALID_SIGNATURE, Status.JWT_INVALID, Status.FORBIDDEN_OPERATION);
  private final KafkaEventsFacade kafkaEventsFacade;

  private static final String CREATE = "KAFKA REQUEST CREATE";
  private static final String READ = "KAFKA REQUEST READ";
  private static final String UPDATE = "KAFKA REQUEST UPDATE";
  private static final String DELETE = "KAFKA REQUEST DELETE";
  private static final String SEARCH = "KAFKA REQUEST SEARCH";

  static final String BEFORE = "BEFORE";
  static final String AFTER = "AFTER";

  public AuditKafkaEventsAspect(KafkaEventsFacade kafkaEventsFacade) {
    this.kafkaEventsFacade = kafkaEventsFacade;
  }

  @Around("kafkaCreate() && args(.., request)")
  Object auditKafkaCreate(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
    return prepareAndSendKafkaAudit(joinPoint, request, CREATE);
  }

  @Around("kafkaRead() && args(.., request)")
  Object auditKafkaRead(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
    return prepareAndSendKafkaAudit(joinPoint, request, READ);
  }

  @Around("kafkaUpdate() && args(.., request)")
  Object auditKafkaUpdate(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
    return prepareAndSendKafkaAudit(joinPoint, request, UPDATE);
  }

  @Around("kafkaDelete() && args(.., request)")
  Object auditKafkaDelete(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
    return prepareAndSendKafkaAudit(joinPoint, request, DELETE);
  }

  @Around("kafkaSearch() && args(.., request)")
  Object auditKafkaSearch(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
    return prepareAndSendKafkaAudit(joinPoint, request, SEARCH);
  }

  private Object prepareAndSendKafkaAudit(ProceedingJoinPoint joinPoint, Request<?> request,
      String action) throws Throwable {

    EventType eventType = EventType.USER_ACTION;
    String methodName = joinPoint.getSignature().getName();
    kafkaEventsFacade.sendKafkaAudit(eventType, methodName, request, action, BEFORE, null);

    Object result = joinPoint.proceed();
    var resultStatus = ((Message<Response<?>>) result).getPayload().getStatus();

    if (resultStatus != null && auditableStatusToHandler.contains(resultStatus)) {
      eventType = EventType.SECURITY_EVENT;
    }
    kafkaEventsFacade.sendKafkaAudit(
        eventType,
        methodName,
        request,
        action,
        AFTER,
        Optional.ofNullable(resultStatus).map(Objects::toString).orElse(null));
    return result;
  }
}
