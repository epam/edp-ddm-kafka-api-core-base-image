/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.kafkaapi.core.exception.SequenceGeneratorException;
import com.epam.digital.data.platform.kafkaapi.core.util.SQLExceptionResolverUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SequenceGenerator {

  private static final String REQUEST_TEMPLATE = "SELECT nextval('%s');";

  private final Logger log = LoggerFactory.getLogger(SequenceGenerator.class);
  private final DataSource dataSource;

  public SequenceGenerator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public Long nextValue(String sequenceName) {
    log.info("Generate next value by sequence {}", sequenceName);

    try (Connection connection = DataSourceUtils.getConnection(this.dataSource);
        var statement = connection.prepareStatement(
            String.format(REQUEST_TEMPLATE, sequenceName))
    ) {
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getLong(1);
      } else {
        throw new SequenceGeneratorException("Generation of next value failed. Next value absent");
      }
    } catch (SQLException e) {
      throw SQLExceptionResolverUtil.getDetailedExceptionFromSql(e);
    }
  }
}
