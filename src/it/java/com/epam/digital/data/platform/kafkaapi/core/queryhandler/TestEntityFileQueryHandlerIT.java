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

package com.epam.digital.data.platform.kafkaapi.core.queryhandler;

import static com.epam.digital.data.platform.kafkaapi.core.util.DaoTestUtils.TEST_ENTITY_FILE;
import static com.epam.digital.data.platform.kafkaapi.core.util.SecurityUtils.mockSecurityContext;

import com.epam.digital.data.platform.kafkaapi.core.config.TestConfiguration;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityFile;
import com.epam.digital.data.platform.kafkaapi.core.impl.queryhandler.TestEntityFileQueryHandler;
import com.epam.digital.data.platform.kafkaapi.core.service.AccessPermissionService;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.nimbusds.jose.JOSEException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestConfiguration
@SpringBootTest(
    classes = {
      TestEntityFileQueryHandler.class,
      AccessPermissionService.class,
      JwtInfoProvider.class,
      TokenParser.class
    })
class TestEntityFileQueryHandlerIT {

  @Autowired
  TestEntityFileQueryHandler queryHandler;

  TestEntityFile entityFile = TEST_ENTITY_FILE;

  @Test
  @DisplayName("Find by ID")
  void findById() throws JOSEException {
    Optional<TestEntityFile> found =
        queryHandler.findById(
            new Request<>(
                entityFile.getId(),
                null,
                mockSecurityContext()));
    Assertions.assertThat(found).isPresent();
    Assertions.assertThat(found.get().getLegalEntityName()).isEqualTo(entityFile.getLegalEntityName());
    Assertions.assertThat(found.get().getScanCopy().getId()).isEqualTo(entityFile.getScanCopy().getId());
    Assertions.assertThat(found.get().getScanCopy().getChecksum()).isEqualTo(entityFile.getScanCopy().getChecksum());
  }
}
