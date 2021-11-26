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

package com.epam.digital.data.platform.kafkaapi.core.aspect;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.impl.CommandHandlerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.listener.impl.GenericQueryListenerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.queryhandler.impl.QueryHandlerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.service.DigitalSignatureService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtValidationService;
import com.epam.digital.data.platform.kafkaapi.core.service.ResponseMessageCreator;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.starter.actuator.livenessprobe.LivenessStateHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

@Import({AopAutoConfiguration.class})
@SpringBootTest(classes = {GenericQueryListenerTestImpl.class, LivenessStateCheckAspect.class})
class LivenessStateCheckAspectTest {

  @Autowired
  private GenericQueryListenerTestImpl genericListener;
  @MockBean
  private JwtValidationService jwtValidationService;
  @MockBean
  private JwtInfoProvider jwtInfoProvider;
  @MockBean
  private QueryHandlerTestImpl queryHandler;
  @MockBean
  private CommandHandlerTestImpl commandHandler;
  @MockBean
  private DigitalSignatureService digitalSignatureService;
  @MockBean
  private LivenessStateHandler livenessStateHandler;
  @MockBean
  private ResponseMessageCreator responseMessageCreator;

  @Test
  void expectStateHandlerIsCalledAfterKafkaListener() {
    when(responseMessageCreator.createMessageByPayloadSize(any()))
            .thenReturn(MessageBuilder.withPayload(new Response<>()).build());

    genericListener.create("", new Request<>());

    verify(livenessStateHandler).handleResponse(any(), any());
  }
}