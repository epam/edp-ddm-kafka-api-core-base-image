package com.epam.digital.data.platform.kafkaapi.core.impl.commandhandler;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityM2M;
import org.springframework.stereotype.Service;

@Service
public class TestEntityM2MCommandHandler extends AbstractCommandHandler<TestEntityM2M> {

  public TestEntityM2MCommandHandler(EntityConverter<TestEntityM2M> entityConverter) {
    super(entityConverter);
  }

  @Override
  public String tableName() {
    return "test_entity_m2m";
  }

  @Override
  public String pkColumnName() {
    return "id";
  }
}
