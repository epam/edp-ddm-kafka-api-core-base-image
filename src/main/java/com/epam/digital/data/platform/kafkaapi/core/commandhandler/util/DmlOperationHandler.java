package com.epam.digital.data.platform.kafkaapi.core.commandhandler.util;

import com.epam.digital.data.platform.kafkaapi.core.annotation.DatabaseOperation;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.model.DmlOperationArgs;
import com.epam.digital.data.platform.kafkaapi.core.exception.ProcedureErrorException;
import com.epam.digital.data.platform.kafkaapi.core.util.JwtClaimsUtils;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.kafkaapi.core.util.SQLExceptionResolverUtil;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.postgresql.util.HStoreConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DmlOperationHandler {

  private static final String INSERT_ID_COLUMN = "f_row_insert";

  private final Logger log = LoggerFactory.getLogger(DmlOperationHandler.class);

  private final DataSource dataSource;

  public DmlOperationHandler(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @DatabaseOperation(Operation.CREATE)
  public String save(DmlOperationArgs args) {
    log.info("Inserting into DB");

    try (Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall(DmlOperation.I.getSqlString())) {
      Array rolesDbArray = connection
          .createArrayOf("text", JwtClaimsUtils.getRoles(args.getUserClaims()).toArray());
      statement.setString(1, args.getTableName());
      statement.setString(2, HStoreConverter.toString(args.getSysValues()));
      statement.setString(3, HStoreConverter.toString(args.getBusinessValues()));
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

  @DatabaseOperation(Operation.UPDATE)
  public void update(DmlOperationArgs args) {
    log.info("Updating in DB");

    try (Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall(DmlOperation.U.getSqlString())) {
      Array rolesDbArray = connection
          .createArrayOf("text", JwtClaimsUtils.getRoles(args.getUserClaims()).toArray());
      statement.setString(1, args.getTableName());
      statement.setString(2, args.getEntityId());
      statement.setString(3, HStoreConverter.toString(args.getSysValues()));
      statement.setString(4, HStoreConverter.toString(args.getBusinessValues()));
      statement.setArray(5, rolesDbArray);

      statement.execute();
    } catch (SQLException e) {
      throw SQLExceptionResolverUtil.getDetailedExceptionFromSql(e);
    }
  }

  @DatabaseOperation(Operation.DELETE)
  public void delete(DmlOperationArgs args) {
    log.info("Deleting from DB");

    try (Connection connection = dataSource.getConnection();
        CallableStatement statement = connection.prepareCall(DmlOperation.D.getSqlString())) {
      Array rolesDbArray = connection
          .createArrayOf("text", JwtClaimsUtils.getRoles(args.getUserClaims()).toArray());
      statement.setString(1, args.getTableName());
      statement.setString(2, args.getEntityId());
      statement.setString(3, HStoreConverter.toString(args.getSysValues()));
      statement.setArray(4, rolesDbArray);

      statement.execute();
    } catch (SQLException e) {
      throw SQLExceptionResolverUtil.getDetailedExceptionFromSql(e);
    }
  }
}
