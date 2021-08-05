package com.epam.digital.data.platform.kafkaapi.core.listener.search.impl;

import com.epam.digital.data.platform.kafkaapi.core.annotation.KafkaAudit;
import com.epam.digital.data.platform.kafkaapi.core.listener.GenericSearchListener;
import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntityContains;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import java.util.List;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.messaging.Message;

@TestComponent
public class GenericSearchListenerTestImpl
    extends GenericSearchListener<MockEntityContains, MockEntity> {

  protected GenericSearchListenerTestImpl(
      AbstractSearchHandler<MockEntityContains, MockEntity> searchHandler) {
    super(searchHandler);
  }

  @KafkaAudit(Operation.SEARCH)
  @Override
  public Message<Response<List<MockEntity>>> search(String key, Request<MockEntityContains> input) {
    return super.search(key, input);
  }
}
