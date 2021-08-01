package com.epam.digital.data.platform.kafkaapi.core.aspect;

import org.aspectj.lang.annotation.Pointcut;

public interface DbUpdateAspect {

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.queryhandler.AbstractQueryHandler.findById(..))")
  default void findById() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler.save(..))")
  default void save() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler.update(..))")
  default void update() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler.delete(..))")
  default void delete() {}

  @Pointcut("execution(public * com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler.search(..))")
  default void search() {}
}
