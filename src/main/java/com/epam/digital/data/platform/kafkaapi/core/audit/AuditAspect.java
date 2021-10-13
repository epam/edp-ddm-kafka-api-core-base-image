package com.epam.digital.data.platform.kafkaapi.core.audit;

import com.epam.digital.data.platform.kafkaapi.core.annotation.DatabaseOperation;
import com.epam.digital.data.platform.kafkaapi.core.annotation.KafkaAudit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

  private final DatabaseAuditProcessor databaseAuditProcessor;
  private final KafkaAuditProcessor kafkaAuditProcessor;

  public AuditAspect(DatabaseAuditProcessor databaseAuditProcessor,
      KafkaAuditProcessor kafkaAuditProcessor) {
    this.databaseAuditProcessor = databaseAuditProcessor;
    this.kafkaAuditProcessor = kafkaAuditProcessor;
  }

  @Around("@annotation(com.epam.digital.data.platform.kafkaapi.core.annotation.DatabaseOperation)")
  Object databaseAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    var signature = (MethodSignature) joinPoint.getSignature();
    var annotation = signature.getMethod().getAnnotation(DatabaseOperation.class);
    var operation = annotation.value();
    return databaseAuditProcessor.process(joinPoint, operation);
  }

  @Around("@annotation(com.epam.digital.data.platform.kafkaapi.core.annotation.KafkaAudit)")
  Object kafkaAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    var signature = (MethodSignature) joinPoint.getSignature();
    var annotation = signature.getMethod().getAnnotation(KafkaAudit.class);
    var operation = annotation.value();
    return kafkaAuditProcessor.process(joinPoint, operation);
  }
}
