package com.epam.digital.data.platform.kafkaapi.core.searchhandler;

import com.epam.digital.data.platform.kafkaapi.core.annotation.DatabaseAudit;
import com.epam.digital.data.platform.kafkaapi.core.exception.SqlErrorException;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.Request;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSearchHandler<I, O> implements SearchHandler<I, O> {

  @Autowired
  protected DSLContext context;

  @DatabaseAudit(Operation.SEARCH)
  @Override
  public List<O> search(Request<I> input) {
    I searchCriteria = input.getPayload();

    try {
      return
          context
              .select(selectFields())
              .from(DSL.table(tableName()))
              .where(whereClause(searchCriteria))
              .limit(offset(searchCriteria), limit(searchCriteria))
              .fetchInto(entityType());
    } catch (Exception e) {
      throw new SqlErrorException("Can not read from DB", e);
    }
  }

  protected abstract Condition whereClause(I searchCriteria);

  public abstract String tableName();

  public abstract Class<O> entityType();

  public abstract List<SelectFieldOrAsterisk> selectFields();

  public Integer limit(I searchCriteria) {
    return null;
  }

  public Integer offset(I searchCriteria) {
    return null;
  }
}
