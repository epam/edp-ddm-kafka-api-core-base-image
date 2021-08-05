package com.epam.digital.data.platform.kafkaapi.core.listener.impl;

import com.epam.digital.data.platform.kafkaapi.core.annotation.KafkaAudit;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.AbstractCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.listener.GenericQueryListener;
import com.epam.digital.data.platform.kafkaapi.core.queryhandler.AbstractQueryHandler;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.EntityId;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import java.util.UUID;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;

@TestComponent
public class GenericQueryListenerTestImpl extends GenericQueryListener<UUID, MockEntity> {

  protected GenericQueryListenerTestImpl(
      AbstractCommandHandler<MockEntity> commandHandler,
      AbstractQueryHandler<UUID, MockEntity> queryHandler) {
    super(commandHandler, queryHandler);
  }

  @KafkaAudit(Operation.CREATE)
  @Override
  @KafkaListener
  @SendTo
  public Message<Response<EntityId>> create(
      @Header(name = DIGITAL_SEAL, required = false) String key, Request<MockEntity> input) {
    return super.create(key, input);
  }

  @KafkaAudit(Operation.CREATE)
  @Override
  @KafkaListener
  @SendTo
  public Message<Response<MockEntity>> read(
      @Header(name = DIGITAL_SEAL, required = false) String key, Request<UUID> input) {
    return super.read(key, input);
  }

  @KafkaAudit(Operation.CREATE)
  @Override
  @KafkaListener
  @SendTo
  public Message<Response<Void>> update(
      @Header(name = DIGITAL_SEAL, required = false) String key, Request<MockEntity> input) {
    return super.update(key, input);
  }

  @KafkaAudit(Operation.CREATE)
  @Override
  @KafkaListener
  @SendTo
  public Message<Response<Void>> delete(
      @Header(name = DIGITAL_SEAL, required = false) String key, Request<MockEntity> id) {
    return super.delete(key, id);
  }
}
