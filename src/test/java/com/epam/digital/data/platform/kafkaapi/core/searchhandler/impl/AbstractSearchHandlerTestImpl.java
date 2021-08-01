package com.epam.digital.data.platform.kafkaapi.core.searchhandler.impl;

import com.epam.digital.data.platform.kafkaapi.core.searchhandler.AbstractSearchHandler;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntityContains;
import org.jooq.Condition;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;

import java.util.Collections;
import java.util.List;

public class AbstractSearchHandlerTestImpl extends AbstractSearchHandler<MockEntityContains, MockEntity> {

  private String tableName = "table_name";

  @Override
  protected Condition whereClause(MockEntityContains searchCriteria) {
    return DSL.noCondition();
  }

  @Override
  public String tableName() {
    return tableName;
  }

  @Override
  public Class<MockEntity> entityType() {
    return MockEntity.class;
  }

  @Override
  public List<SelectFieldOrAsterisk> selectFields() {
    return Collections.singletonList(DSL.field("field"));
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
}