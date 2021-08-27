package com.epam.digital.data.platform.kafkaapi.core.audit;

import com.epam.digital.data.platform.kafkaapi.core.exception.AuditException;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class KafkaAuditProcessor implements AuditProcessor<Operation> {

  private final Logger log = LoggerFactory.getLogger(KafkaAuditProcessor.class);

  private static final Set<Status> auditableStatusToHandler =
      Set.of(Status.INVALID_SIGNATURE, Status.JWT_INVALID, Status.FORBIDDEN_OPERATION);

  private static final String CREATE = "KAFKA REQUEST CREATE";
  private static final String READ = "KAFKA REQUEST READ";
  private static final String UPDATE = "KAFKA REQUEST UPDATE";
  private static final String DELETE = "KAFKA REQUEST DELETE";
  private static final String SEARCH = "KAFKA REQUEST SEARCH";

  static final String BEFORE = "BEFORE";
  static final String AFTER = "AFTER";

  private final KafkaEventsFacade kafkaEventsFacade;

  public KafkaAuditProcessor(KafkaEventsFacade kafkaEventsFacade) {
    this.kafkaEventsFacade = kafkaEventsFacade;
  }

  @Override
  public Object process(ProceedingJoinPoint joinPoint, Operation operation) throws Throwable {
    var request = getArgumentByType(joinPoint, Request.class);
    switch (operation) {
      case CREATE:
        return prepareAndSendKafkaAudit(joinPoint, request, CREATE);
      case READ:
        return prepareAndSendKafkaAudit(joinPoint, request, READ);
      case UPDATE:
        return prepareAndSendKafkaAudit(joinPoint, request, UPDATE);
      case DELETE:
        return prepareAndSendKafkaAudit(joinPoint, request, DELETE);
      case SEARCH:
        return prepareAndSendKafkaAudit(joinPoint, request, SEARCH);
      default:
        throw new AuditException("Unsupported audit operation");
    }
  }

  private Object prepareAndSendKafkaAudit(ProceedingJoinPoint joinPoint, Request<?> request,
      String action) throws Throwable {

    EventType eventType = EventType.USER_ACTION;
    String methodName = joinPoint.getSignature().getName();

    log.info("Sending {} event to Audit", action);
    kafkaEventsFacade.sendKafkaAudit(eventType, methodName, request, action, BEFORE, null);

    Object result = joinPoint.proceed();

    var resultStatus = ((Message<Response<?>>) result).getPayload().getStatus();

    if (resultStatus != null && auditableStatusToHandler.contains(resultStatus)) {
      eventType = EventType.SECURITY_EVENT;
    }

    log.info("Sending {} completed event to Audit", action);
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
