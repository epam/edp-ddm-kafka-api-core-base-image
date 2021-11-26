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

import com.epam.digital.data.platform.dso.api.dto.SignRequestDto;
import com.epam.digital.data.platform.dso.api.dto.SignResponseDto;
import com.epam.digital.data.platform.dso.api.dto.VerifyRequestDto;
import com.epam.digital.data.platform.dso.api.dto.VerifyResponseDto;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.integration.ceph.dto.CephObject;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.kafkaapi.core.annotation.DatabaseOperation;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Import({AopAutoConfiguration.class})
@SpringBootTest(
    classes = {
      ExecutionTimeLoggingAspectTest.MockCephService.class,
      ExecutionTimeLoggingAspectTest.MockDigitalSealRestClient.class,
      ExecutionTimeLoggingAspectTest.MockDbClient.class,
      ExecutionTimeLoggingAspectTest.MockKafkaListener.class,
      ExecutionTimeLoggingAspect.class,
    })
class ExecutionTimeLoggingAspectTest {

  @Autowired
  private MockKafkaListener mockKafkaListener;
  @Autowired
  private MockCephService mockCephService;
  @Autowired
  private MockDigitalSealRestClient mockDigitalSealRestClient;
  @Autowired
  private MockDbClient mockDbClient;

  @SpyBean
  private ExecutionTimeLoggingAspect executionTimeLoggingAspect;

  @Test
  void expectCephServiceAspectCalled() throws Throwable {
    mockCephService.deleteObject("", "");
    verify(executionTimeLoggingAspect).logCephCommunicationTime(any());
  }

  @Test
  void expectDsoServiceAspectCalled() throws Throwable {
    mockDigitalSealRestClient.sign(null);

    verify(executionTimeLoggingAspect).logDsoCommunicationTime(any());
  }

  @Test
  void expectDbAspectCalled() throws Throwable {
    mockDbClient.save();

    verify(executionTimeLoggingAspect).logDbCommunicationTime(any());
  }

  @Test
  void expectKafkaListenerAspectCalled() throws Throwable {
    mockKafkaListener.callKafka();

    verify(executionTimeLoggingAspect).logKafkaRequestProcessingTime(any());
  }

  @TestComponent
  static class MockCephService implements CephService {

    @Override
    public Optional<String> getContent(String s, String s1) {
      return Optional.empty();
    }

    @Override
    public Optional<CephObject> getObject(String s, String s1) {
      return Optional.empty();
    }

    @Override
    public void putContent(String s, String s1, String s2) {}

    @Override
    public void putObject(String s, String s1, CephObject cephObject) {}

    @Override
    public void deleteObject(String s, String s1) {}

    @Override
    public boolean doesObjectExist(String s, String s1) {
      return false;
    }
  }

  @TestComponent
  static class MockDigitalSealRestClient implements DigitalSealRestClient {

    @Override
    public VerifyResponseDto verify(VerifyRequestDto verifyRequestDto) {
      return null;
    }

    @Override
    public SignResponseDto sign(SignRequestDto signRequest) {
      return null;
    }
  }

  @TestComponent
  static class MockDbClient {

    @DatabaseOperation(Operation.CREATE)
    public void save() {}
  }

  @TestComponent
  static class MockKafkaListener {

    @KafkaListener
    public void callKafka() {}
  }
}
