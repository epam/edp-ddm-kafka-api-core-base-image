package com.epam.digital.data.platform.kafkaapi.core.commandhandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.config.TestConfiguration;
import com.epam.digital.data.platform.kafkaapi.core.impl.commandhandler.TestEntityFileCommandHandler;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityFile;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.util.DaoTestUtils;
import com.epam.digital.data.platform.kafkaapi.core.util.SecurityUtils;
import com.epam.digital.data.platform.model.core.kafka.File;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.RequestContext;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestConfiguration
@SpringBootTest(
    classes = {
        TestEntityFileCommandHandler.class,
        EntityConverter.class,
        DmlOperationHandler.class,
        JwtInfoProvider.class,
        TokenParser.class
    })
class TestEntityFileCommandHandlerIT {

  static final String EXISTING_RECORD_ID = "3cc262c1-0cd8-4d45-be66-eb0fca821e0a";
  static final String TYPICAL_UUID = "123e4567-e89b-12d3-a456-426655440000";

  @Autowired
  TestEntityFileCommandHandler commandHandler;

  TestEntityFile newTestRecord;
  Request<TestEntityFile> newTestRequest;
  TestEntityFile existingTestRecord;
  Request<TestEntityFile> existingTestRequest;

  @BeforeEach
  void setUp() throws JOSEException {
    newTestRecord = new TestEntityFile();
    newTestRecord.setLegalEntityName("Kurylo Volodymyr");
    newTestRecord.setScanCopy(new File(TYPICAL_UUID, "checksum"));

    existingTestRecord = DaoTestUtils.testEntityFile();

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
    existingTestRecord.setLegalEntityName(updatedName);

    assertDoesNotThrow(() -> commandHandler.update(existingTestRequest));
  }

  @Test
  @DisplayName("Delete record has no errors on processing")
  void expectNoErrorsWhenDelete() {
    assertDoesNotThrow(() -> commandHandler.delete(existingTestRequest));
  }
}
