package com.epam.digital.data.platform.kafkaapi.core.commandhandler.util;

import com.epam.digital.data.platform.kafkaapi.core.exception.ProcedureErrorException;
import com.epam.digital.data.platform.kafkaapi.core.util.JwtClaimsUtils;
import com.epam.digital.data.platform.kafkaapi.core.util.SQLExceptionResolverUtil;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import org.postgresql.util.HStoreConverter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class DmlOperationHandler {

  private static final String INSERT_ID_COLUMN = "f_row_insert";

  private final DataSource dataSource;

  public DmlOperationHandler(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public String save(
      String tableName,
      JwtClaimsDto userClaims,
      Map<String, String> sysValues,
      Map<String, Object> businessValues) {
    try (Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall(DmlOperation.I.getSqlString())) {
      Array rolesDbArray = connection.createArrayOf("text", JwtClaimsUtils.getRoles(userClaims).toArray());
      statement.setString(1, tableName);
      statement.setString(2, HStoreConverter.toString(sysValues));
      statement.setString(3, HStoreConverter.toString(businessValues));
      statement.setArray(4, rolesDbArray);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getString(INSERT_ID_COLUMN);
      } else {
        throw new ProcedureErrorException(
            "Inserted ID is not returned from procedure in column " + INSERT_ID_COLUMN);
      }
    } catch (SQLException e) {
      throw SQLExceptionResolverUtil.getDetailedExceptionFromSql(e);
    }
  }

  public void update(
      String tableName,
      JwtClaimsDto userClaims,
      String entityId,
      Map<String, String> sysValues,
      Map<String, Object> businessValues) {
    try (Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall(DmlOperation.U.getSqlString())) {
      Array rolesDbArray = connection.createArrayOf("text", JwtClaimsUtils.getRoles(userClaims).toArray());
      statement.setString(1, tableName);
      statement.setString(2, entityId);
      statement.setString(3, HStoreConverter.toString(sysValues));
      statement.setString(4, HStoreConverter.toString(businessValues));
      statement.setArray(5, rolesDbArray);
      statement.execute();
    } catch (SQLException e) {
      throw SQLExceptionResolverUtil.getDetailedExceptionFromSql(e);
    }
  }

  public void delete(
      String tableName, JwtClaimsDto userClaims, String entityId, Map<String, String> sysValues) {
    try (Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall(DmlOperation.D.getSqlString())) {
      Array rolesDbArray = connection.createArrayOf("text", JwtClaimsUtils.getRoles(userClaims).toArray());
      statement.setString(1, tableName);
      statement.setString(2, entityId);
      statement.setString(3, HStoreConverter.toString(sysValues));
      statement.setArray(4, rolesDbArray);
      statement.execute();
    } catch (SQLException e) {
      throw SQLExceptionResolverUtil.getDetailedExceptionFromSql(e);
    }
  }
}
