/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.kafkaapi.core.listener.impl;

import com.epam.digital.data.platform.kafkaapi.core.audit.AuditableListener;
import com.epam.digital.data.platform.kafkaapi.core.listener.GenericSearchListener;
import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntityContains;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import java.util.List;

import com.epam.digital.data.platform.model.core.search.SearchConditionPage;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.messaging.Message;

@TestComponent
public class GenericSearchListenerTestImpl
    extends GenericSearchListener<MockEntityContains, MockEntity, List<MockEntity>> {

  protected GenericSearchListenerTestImpl(
      AbstractSearchHandler<MockEntityContains, MockEntity> searchHandler) {
    super(searchHandler);
  }

  @AuditableListener(Operation.SEARCH)
  @Override
  public Message<Response<List<MockEntity>>> search(String key, Request<MockEntityContains> input) {
    return super.search(key, input);
  }

  @Override
  protected List<MockEntity> getResponsePayload(SearchConditionPage<MockEntity> page) {
    return page.getContent();
  }
}

