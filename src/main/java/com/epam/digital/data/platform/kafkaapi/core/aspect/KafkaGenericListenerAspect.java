package com.epam.digital.data.platform.kafkaapi.core.aspect;

import org.aspectj.lang.annotation.Pointcut;

public interface KafkaGenericListenerAspect {

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.listener.GenericQueryListener.create(..))")
  default void kafkaCreate() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.listener.GenericQueryListener.read(..))")
  default void kafkaRead() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.listener.GenericQueryListener.update(..))")
  default void kafkaUpdate() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.listener.GenericQueryListener.delete(..))")
  default void kafkaDelete() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.listener.GenericSearchListener.search(..))")
  default void kafkaSearch() {}
}