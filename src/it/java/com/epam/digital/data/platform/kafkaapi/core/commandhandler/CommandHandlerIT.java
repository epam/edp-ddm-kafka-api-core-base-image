package com.epam.digital.data.platform.kafkaapi.core.commandhandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.config.TestConfiguration;
import com.epam.digital.data.platform.kafkaapi.core.impl.commandhandler.TestEntityCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntity;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.util.DaoTestUtils;
import com.epam.digital.data.platform.kafkaapi.core.util.SecurityUtils;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.RequestContext;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.nimbusds.jose.JOSEException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestConfiguration
@SpringBootTest(
    classes = {
        TestEntityCommandHandler.class,
        EntityConverter.class,
        DmlOperationHandler.class,
        JwtInfoProvider.class,
        TokenParser.class
    })
class CommandHandlerIT {

  static final String EXISTING_RECORD_ID = "3cc262c1-0cd8-4d45-be66-eb0fca821e0a";

  @Autowired
  TestEntityCommandHandler commandHandler;

  TestEntity newTestRecord;
  Request<TestEntity> newTestRequest;
  TestEntity existingTestRecord;
  Request<TestEntity> existingTestRequest;

  @BeforeEach
  void setUp() throws JOSEException {
    newTestRecord = new TestEntity();
    newTestRecord.setConsentDate(LocalDateTime.now());
    newTestRecord.setPersonFullName("Kurylo Volodymyr");

    existingTestRecord = DaoTestUtils.testEntity();

    newTestRequest = new Request<>(newTestRecord, new RequestContext(), SecurityUtils.mockSecurityContext());
    existingTestRequest =
        new Request<>(existingTestRecord, new RequestContext(), SecurityUtils.mockSecurityContext());
  }

  @Test
  @DisplayName("Save new record has no errors on processing")
  void expectNoErrorsWhenSave() {
    Assertions.assertDoesNotThrow(() -> commandHandler.save(newTestRequest));
  }

  @Test
  @DisplayName("Update record has no errors on processing")
  void expectNoErrorsWhenUpdate() {
    String updatedName = "Test User";
    existingTestRecord.setPersonFullName(updatedName);

    assertDoesNotThrow(() -> commandHandler.update(existingTestRequest));
  }

  @Test
  @DisplayName("Delete record has no errors on processing")
  void expectNoErrorsWhenDelete() {
    assertDoesNotThrow(() -> commandHandler.delete(existingTestRequest));
  }
}
