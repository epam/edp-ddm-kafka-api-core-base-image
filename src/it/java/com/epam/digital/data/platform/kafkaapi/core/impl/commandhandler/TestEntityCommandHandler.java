package com.epam.digital.data.platform.kafkaapi.core.impl.commandhandler;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntity;
import org.springframework.stereotype.Service;

@Service
public class TestEntityCommandHandler extends AbstractCommandHandler<TestEntity> {

  public TestEntityCommandHandler(EntityConverter<TestEntity> entityConverter) {
    super(entityConverter);
  }

  @Override
  public String tableName() {
    return "test_entity";
  }

  @Override
  public String pkColumnName() {
    return "id";
  }
}
