package com.epam.digital.data.platform.kafkaapi.core.commandhandler.util;

public enum DmlOperation {
  I("select f_row_insert(?, (?)::hstore, (?)::hstore, ?);"),
  U("call p_row_update(?, ?::UUID, (?)::hstore, (?)::hstore, ?);"),
  D("call p_row_delete(?, ?::UUID, (?)::hstore, ?);");

  private String sql;

  DmlOperation(String sql) {
    this.sql = sql;
  }

  public String getSqlString() {
    return sql;
  }
}
