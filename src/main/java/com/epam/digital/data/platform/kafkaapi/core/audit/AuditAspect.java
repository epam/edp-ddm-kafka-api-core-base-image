/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
