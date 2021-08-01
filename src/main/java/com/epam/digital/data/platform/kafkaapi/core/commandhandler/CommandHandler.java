package com.epam.digital.data.platform.kafkaapi.core.commandhandler;

import com.epam.digital.data.platform.model.core.kafka.EntityId;
import com.epam.digital.data.platform.model.core.kafka.Request;
import org.springframework.cloud.sleuth.annotation.NewSpan;

public interface CommandHandler<T> {

  @NewSpan
  EntityId save(Request<T> input);

  @NewSpan
  void update(Request<T> input);

  @NewSpan
  void delete(Request<T> input);
}
