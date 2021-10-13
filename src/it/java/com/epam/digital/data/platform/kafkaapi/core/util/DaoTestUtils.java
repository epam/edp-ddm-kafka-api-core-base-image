package com.epam.digital.data.platform.kafkaapi.core.util;

import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntity;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityFile;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityM2M;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TypGender;
import com.epam.digital.data.platform.model.core.kafka.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DaoTestUtils {

  public static final UUID TEST_ENTITY_ID = UUID
      .fromString("3cc262c1-0cd8-4d45-be66-eb0fca821e0a");
  public static final UUID TEST_ENTITY_ID_2 = UUID
      .fromString("9ce4cad9-ff50-4fa3-b893-e07afea0cb8d");
  public static final LocalDateTime PD_PROCESSING_CONSENT_CONSENT_DATE = LocalDateTime
      .of(2020, 1, 15, 12, 0, 1);
  public static final UUID TEST_ENTITY_FILE_ID = UUID
      .fromString("7f017d37-6ba5-4849-a4b2-f6a3ef2cadb9");
  public static final UUID TEST_ENTITY_M2M_ID = UUID
      .fromString("7f017d37-6ba5-4849-a4b2-f6a3ef2cadb9");

  public static final TestEntity TEST_ENTITY = testEntity();
  public static final TestEntity TEST_ENTITY_2 = testEntity2();
  public static final TestEntityFile TEST_ENTITY_FILE = testEntityFile();
  public static final TestEntityM2M TEST_ENTITY_M2M = testEntityM2M();

  private DaoTestUtils() {
  }

  public static TestEntity testEntity() {
    var r = new TestEntity();
    r.setId(TEST_ENTITY_ID);
    r.setPersonFullName("John Doe Patronymic");
    r.setPersonPassNumber("AB123456");
    r.setConsentDate(PD_PROCESSING_CONSENT_CONSENT_DATE);
    r.setPersonGender(TypGender.M);
    return r;
  }

  public static TestEntity testEntity2() {
    var r = new TestEntity();
    r.setId(TEST_ENTITY_ID_2);
    r.setPersonFullName("Benjamin Franklin Patronymic");
    r.setPersonPassNumber("XY098765");
    r.setConsentDate(PD_PROCESSING_CONSENT_CONSENT_DATE);
    r.setPersonGender(TypGender.M);
    return r;
  }

  public static TestEntityFile testEntityFile() {
    var r = new TestEntityFile();
    r.setId(TEST_ENTITY_FILE_ID);
    r.setLegalEntityName("FOP John Doe");
    r.setScanCopy(new File("1", "0d5f97dd25b50267a1c03fba4d649d56d3d818704d0dcdfa692db62119b1221a"));
    return r;
  }

  public static TestEntityM2M testEntityM2M() {
    var r = new TestEntityM2M();
    r.setId(TEST_ENTITY_M2M_ID);
    r.setName("FOP John Doe");
    r.setEntities(List.of(TEST_ENTITY_ID, TEST_ENTITY_ID_2));
    return r;
  }
}