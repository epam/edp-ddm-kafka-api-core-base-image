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

package com.epam.digital.data.platform.kafkaapi.core.commandhandler.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.kafkaapi.core.config.GenericConfig;
import com.epam.digital.data.platform.kafkaapi.core.config.JooqTestConfig;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.model.core.geometry.Dot;
import com.epam.digital.data.platform.model.core.geometry.Line;
import com.epam.digital.data.platform.model.core.geometry.Point;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntityMultiFiles;
import com.epam.digital.data.platform.model.core.geometry.Polygon;
import com.epam.digital.data.platform.model.core.kafka.File;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.RequestContext;
import com.epam.digital.data.platform.model.core.kafka.SecurityContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = {
    EntityConverter.class,
    GenericConfig.class
})
@ContextConfiguration(classes = JooqTestConfig.class)
class EntityConverterTest {

  private static final UUID CONSENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
  private static final LocalDateTime CONSENT_DATE = LocalDateTime.MIN;
  private static final String USER_NAME = "Name mock";
  private static final String USER_PASS = "Pass mock";
  private static final String SCAN_COPY_ID = UUID.randomUUID().toString();
  private static final String SCAN_COPY_CHECKSUM = "Mock checksum";

  private static final String SOURCE_SYSTEM = "Source system";
  private static final String SOURCE_APPLICATION = "Source application";
  private static final String SOURCE_PROCESS = "Source process";
  private static final String BUSINESS_ACTIVITY = "Source activity";
  private static final String PROCESS_DEFINITION_ID = "Process definition id";
  private static final String PROCESS_INSTANCE_ID = "Process instance id";
  private static final String ACTIVITY_INSTANCE_ID = "Activity instance id";

  private static final String DIGITAL_SIGNATURE = "digital_sign";
  private static final String DIGITAL_SIGNATURE_DERIVED = "digital_sign_derived";

  private static final RequestContext context = new RequestContext();
  private static final SecurityContext securityContext = new SecurityContext();

  private static final String USER_ID = "3fa85f64-5717-2222-b3fc-2c963f66afa6";

  @Autowired
  private EntityConverter<MockEntity> entityConverter;

  @Autowired
  private EntityConverter<MockEntityMultiFiles> entityConverterMultiFiles;

  @BeforeAll
  static void init() {
    context.setBusinessActivity(BUSINESS_ACTIVITY);
    context.setApplication(SOURCE_APPLICATION);
    context.setBusinessProcess(SOURCE_PROCESS);
    context.setSystem(SOURCE_SYSTEM);
    context.setBusinessProcessDefinitionId(PROCESS_DEFINITION_ID);
    context.setBusinessProcessInstanceId(PROCESS_INSTANCE_ID);
    context.setBusinessActivityInstanceId(ACTIVITY_INSTANCE_ID);

    securityContext.setDigitalSignature(DIGITAL_SIGNATURE);
    securityContext.setDigitalSignatureDerived(DIGITAL_SIGNATURE_DERIVED);
  }

  @Test
  void expectEntityIsMappedToDbFields() {
    MockEntity mockEntity = getMockEntity();
    Point location = new Point(BigDecimal.valueOf(1.1), BigDecimal.valueOf(1.3));
    mockEntity.setLocation(location);

    Map<String, Object> entityMap = entityConverter.entityToMap(mockEntity);

    assertThat(entityMap)
        .hasSize(6)
        .containsEntry("consent_id", CONSENT_ID.toString())
        .containsEntry(
            "consent_date",
            CONSENT_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
        .containsEntry("person_full_name", USER_NAME)
        .containsEntry("person_pass_number", USER_PASS)
        .containsEntry("passport_scan_copy", "(" + SCAN_COPY_ID + "," + SCAN_COPY_CHECKSUM + ")")
        .containsEntry("location", "SRID=4326;POINT(1.1 1.3)");
  }

  @Test
  void expectEntityIsMappedWithLineField() {
    MockEntity mockEntity = getMockEntity();
    mockEntity.setLocation(
        new Line(
            List.of(
                new Dot(BigDecimal.valueOf(1.1), BigDecimal.valueOf(1.3)),
                new Dot(BigDecimal.valueOf(10.75), BigDecimal.valueOf(15.17)))));

    Map<String, Object> entityMap = entityConverter.entityToMap(mockEntity);

    assertThat(entityMap)
            .hasSize(6)
            .containsEntry("consent_id", CONSENT_ID.toString())
            .containsEntry(
                    "consent_date",
                    CONSENT_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
            .containsEntry("person_full_name", USER_NAME)
            .containsEntry("person_pass_number", USER_PASS)
            .containsEntry("passport_scan_copy", "(" + SCAN_COPY_ID + "," + SCAN_COPY_CHECKSUM + ")")
            .containsEntry("location", "SRID=4326;LINESTRING(1.1 1.3, 10.75 15.17)");
  }

  @Test
  void expectEntityIsMappedWithPolygonField() {
    MockEntity mockEntity = getMockEntity();
    mockEntity.setLocation(
            new Polygon(
                    List.of(
                            new Dot(BigDecimal.valueOf(1.1), BigDecimal.valueOf(1.3)),
                            new Dot(BigDecimal.valueOf(10.75), BigDecimal.valueOf(15.17)))));

    Map<String, Object> entityMap = entityConverter.entityToMap(mockEntity);

    assertThat(entityMap)
            .hasSize(6)
            .containsEntry("consent_id", CONSENT_ID.toString())
            .containsEntry(
                    "consent_date",
                    CONSENT_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
            .containsEntry("person_full_name", USER_NAME)
            .containsEntry("person_pass_number", USER_PASS)
            .containsEntry("passport_scan_copy", "(" + SCAN_COPY_ID + "," + SCAN_COPY_CHECKSUM + ")")
            .containsEntry("location", "SRID=4326;POLYGON((1.1 1.3, 10.75 15.17))");
  }

  @Test
  void expectEntityWithInnerMapIsMappedToDbFields() {
    var mockEntity = getMockEntityMultiFiles();
    Map<String, Object> entityMap = entityConverterMultiFiles.entityToMap(mockEntity);

    assertThat(entityMap)
        .hasSize(5)
        .containsEntry("consent_id", CONSENT_ID.toString())
        .containsEntry(
            "consent_date",
            CONSENT_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
        .containsEntry("person_full_name", USER_NAME)
        .containsEntry("person_pass_number", USER_PASS)
        .containsEntry("scan_copies", "{\"(id1,sum1)\",\"(id2,sum2)\",\"(id3,sum3)\"}");
  }

  @Test
  void expectSysValuesFilledFromInput() {
    Map<String, String> sysValuesMapped =
        entityConverter.buildSysValues(USER_ID, new Request<>(null, context, securityContext));

    assertThat(sysValuesMapped)
        .hasSize(10)
        .containsEntry("business_activity", BUSINESS_ACTIVITY)
        .containsEntry("source_system", SOURCE_SYSTEM)
        .containsEntry("source_process", SOURCE_PROCESS)
        .containsEntry("curr_user", USER_ID)
        .containsEntry("source_application", SOURCE_APPLICATION)
        .containsEntry("source_process_definition_id", PROCESS_DEFINITION_ID)
        .containsEntry("source_process_instance_id", PROCESS_INSTANCE_ID)
        .containsEntry("source_activity_instance_id", ACTIVITY_INSTANCE_ID)
        .containsEntry("digital_sign", DIGITAL_SIGNATURE)
        .containsEntry("digital_sign_derived", DIGITAL_SIGNATURE_DERIVED);
  }

  private MockEntity getMockEntity() {
    MockEntity mockEntity = new MockEntity();
    mockEntity.setConsentId(CONSENT_ID);
    mockEntity.setConsentDate(CONSENT_DATE);
    mockEntity.setPersonFullName(USER_NAME);
    mockEntity.setPersonPassNumber(USER_PASS);
    mockEntity.setPassportScanCopy(new File());
    mockEntity.getPassportScanCopy().setId(SCAN_COPY_ID);
    mockEntity.getPassportScanCopy().setChecksum(SCAN_COPY_CHECKSUM);
    return mockEntity;
  }

  private MockEntityMultiFiles getMockEntityMultiFiles() {
    MockEntityMultiFiles mockEntity = new MockEntityMultiFiles();
    mockEntity.setConsentId(CONSENT_ID);
    mockEntity.setConsentDate(CONSENT_DATE);
    mockEntity.setPersonFullName(USER_NAME);
    mockEntity.setPersonPassNumber(USER_PASS);

    var file1 = new File("id1", "sum1");
    var file2 = new File("id2", "sum2");
    var file3 = new File("id3", "sum3");
    mockEntity.setScanCopies(List.of(file1, file2, file3));

    return mockEntity;
  }
}
