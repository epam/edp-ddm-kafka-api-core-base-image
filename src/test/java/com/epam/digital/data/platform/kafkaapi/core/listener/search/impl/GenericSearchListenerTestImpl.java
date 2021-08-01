package com.epam.digital.data.platform.kafkaapi.core.listener.search.impl;

import com.epam.digital.data.platform.kafkaapi.core.listener.GenericSearchListener;
import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntityContains;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class GenericSearchListenerTestImpl
    extends GenericSearchListener<MockEntityContains, MockEntity> {

  protected GenericSearchListenerTestImpl(
      AbstractSearchHandler<MockEntityContains, MockEntity> searchHandler) {
    super(searchHandler);
  }
}
