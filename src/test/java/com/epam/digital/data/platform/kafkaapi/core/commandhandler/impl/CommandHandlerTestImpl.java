package com.epam.digital.data.platform.kafkaapi.core.commandhandler.impl;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class CommandHandlerTestImpl extends AbstractCommandHandler<MockEntity> {
  public CommandHandlerTestImpl(
      EntityConverter<MockEntity> entityConverter) {
    super(entityConverter);
  }

  @Override
  public String tableName() {
    return "table";
  }

  @Override
  public String pkColumnName() {
    return "consent_id";
  }
}
