package com.epam.digital.data.platform.kafkaapi.core.impl.commandhandler;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityFile;
import org.springframework.stereotype.Service;

@Service
public class TestEntityFileCommandHandler extends AbstractCommandHandler<TestEntityFile> {

  public TestEntityFileCommandHandler(EntityConverter<TestEntityFile> entityConverter) {
    super(entityConverter);
  }

  @Override
  public String tableName() {
    return "test_entity_file";
  }

  @Override
  public String pkColumnName() {
    return "id";
  }
}
