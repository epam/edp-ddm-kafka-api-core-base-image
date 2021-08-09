package com.epam.digital.data.platform.kafkaapi.core.queryhandler;

import com.epam.digital.data.platform.kafkaapi.core.annotation.DatabaseAudit;
import com.epam.digital.data.platform.kafkaapi.core.exception.ForbiddenOperationException;
import com.epam.digital.data.platform.kafkaapi.core.exception.SqlErrorException;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.service.AccessPermissionService;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractQueryHandler<I, O> implements QueryHandler<I, O> {

  @Autowired
  protected DSLContext context;
  @Autowired
  protected JwtInfoProvider jwtInfoProvider;

  protected final AccessPermissionService<O> accessPermissionService;

  protected AbstractQueryHandler(AccessPermissionService<O> accessPermissionService) {
    this.accessPermissionService = accessPermissionService;
  }

  @DatabaseAudit(Operation.READ)
  @Override
  public Optional<O> findById(Request<I> input) {
    JwtClaimsDto userClaims = jwtInfoProvider.getUserClaims(input);
    if (!accessPermissionService.hasReadAccess(tableName(), userClaims, entityType())) {
      throw new ForbiddenOperationException("User has invalid role for search by ID");
    }

    I id = input.getPayload();
    try {
      final O dto =
          context
              .select(selectFields())
              .from(DSL.table(tableName()))
              .where(DSL.field(idName()).eq(id))
              .fetchOneInto(entityType());
      return Optional.ofNullable(dto);
    } catch (Exception e) {
      throw new SqlErrorException("Can not read from DB", e);
    }
  }

  public abstract String idName();

  public abstract String tableName();

  public abstract Class<O> entityType();

  public abstract List<SelectFieldOrAsterisk> selectFields();
}