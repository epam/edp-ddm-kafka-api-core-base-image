package com.epam.digital.data.platform.kafkaapi.core.impl.searchhandler;

import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityFile;
import com.epam.digital.data.platform.kafkaapi.core.impl.model.TestEntityFileSearchConditions;
import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import java.util.Arrays;
import java.util.List;
import org.jooq.Condition;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;

public class TestEntityFileSearchHandler extends AbstractSearchHandler<
    TestEntityFileSearchConditions,
    TestEntityFile> {

  private static final Integer MAX_LIMIT = 10;

  @Override
  protected Condition whereClause(
      TestEntityFileSearchConditions searchConditions) {
    var c = DSL.noCondition();

    if (searchConditions.getLegalEntityName() != null) {
      c = c.and(DSL.field("legal_entity_name").startsWithIgnoreCase(searchConditions.getLegalEntityName()));
    }

    return c;
  }

  @Override
  public String tableName() {
    return "test_entity_file_by_legal_entity_name_starts_with_v";
  }

  @Override
  public Class<TestEntityFile> entityType() {
    return TestEntityFile.class;
  }

  @Override
  public List<SelectFieldOrAsterisk> selectFields() {
    return Arrays.asList(
        DSL.field("id"),
        DSL.field("legal_entity_name"),
        DSL.field("scan_copy", com.epam.digital.data.platform.kafkaapi.core.util.JooqDataTypes.FILE_DATA_TYPE)
    );
  }
}
