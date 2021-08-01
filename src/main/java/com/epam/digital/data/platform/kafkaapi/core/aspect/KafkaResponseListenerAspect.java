package com.epam.digital.data.platform.kafkaapi.core.aspect;

import com.epam.digital.data.platform.model.core.kafka.Response;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.messaging.Message;

public interface KafkaResponseListenerAspect {

  @Pointcut("@annotation(org.springframework.kafka.annotation.KafkaListener)")
  default void kafkaListenerPointcut() {}

  @AfterReturning(pointcut = "kafkaListenerPointcut()", returning = "response")
  void handleKafkaResponse(Message<? extends Response> response);
}
