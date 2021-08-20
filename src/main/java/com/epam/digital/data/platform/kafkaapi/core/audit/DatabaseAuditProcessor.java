package com.epam.digital.data.platform.kafkaapi.core.audit;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.model.DmlOperationArgs;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.exception.AuditException;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.util.Operation;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import java.util.Optional;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAuditProcessor implements AuditProcessor<Operation> {

  // action
  static final String CREATE = "INSERT INTO TABLE";
  static final String READ = "SELECT FROM TABLE";
  static final String UPDATE = "UPDATE TABLE";
  static final String DELETE = "DELETE FROM TABLE";
  static final String SEARCH = "SEARCH";

  // step
  static final String BEFORE = "BEFORE";
  static final String AFTER = "AFTER";

  private final DatabaseEventsFacade databaseEventsFacade;
  private final JwtInfoProvider jwtInfoProvider;
  private final EntityConverter<Object> entityConverter;

  public DatabaseAuditProcessor(DatabaseEventsFacade databaseEventsFacade,
      JwtInfoProvider jwtInfoProvider, EntityConverter<Object> entityConverter) {
    this.databaseEventsFacade = databaseEventsFacade;
    this.jwtInfoProvider = jwtInfoProvider;
    this.entityConverter = entityConverter;
  }

  @Override
  public Object process(ProceedingJoinPoint joinPoint, Operation operation) throws Throwable {
    switch (operation) {
      case CREATE:
        return create(joinPoint);
      case READ:
        return findById(joinPoint);
      case UPDATE:
        return update(joinPoint);
      case DELETE:
        return delete(joinPoint);
      case SEARCH:
        return search(joinPoint);
      default:
        throw new AuditException("Unsupported audit operation");
    }
  }

  private Object create(ProceedingJoinPoint joinPoint) throws Throwable {
    var args = getArgumentByType(joinPoint, DmlOperationArgs.class);

    var tableName = args.getTableName();
    var businessValues = args.getBusinessValues();
    var userClaims = args.getUserClaims();

    return prepareAndSendDbAudit(joinPoint, tableName, CREATE, userClaims,
        businessValues.keySet(), null);
  }

  private Object findById(ProceedingJoinPoint joinPoint) throws Throwable {
    var request = getArgumentByType(joinPoint, Request.class);
    var userClaims = jwtInfoProvider.getUserClaims(request);
    var entityId = request.getPayload().toString();
    return prepareAndSendDbAudit(joinPoint, null, READ, userClaims, null, entityId);
  }

  private Object update(ProceedingJoinPoint joinPoint) throws Throwable {
    var args = getArgumentByType(joinPoint, DmlOperationArgs.class);

    var tableName = args.getTableName();
    var userClaims = args.getUserClaims();
    var entityId = args.getEntityId();
    var businessValues = args.getBusinessValues();

    return prepareAndSendDbAudit(joinPoint, tableName, UPDATE, userClaims,
        businessValues.keySet(), entityId);
  }

  private Object delete(ProceedingJoinPoint joinPoint) throws Throwable {
    var args = getArgumentByType(joinPoint, DmlOperationArgs.class);

    var tableName = args.getTableName();
    var userClaims = args.getUserClaims();
    var entityId = args.getEntityId();

    return prepareAndSendDbAudit(joinPoint, tableName, DELETE, userClaims, null, entityId);
  }


  private Object search(ProceedingJoinPoint joinPoint) throws Throwable {
    var request = getArgumentByType(joinPoint, Request.class);
    JwtClaimsDto userClaims = jwtInfoProvider.getUserClaims(request);
    Set<String> fields = getFields(request.getPayload());
    return prepareAndSendDbAudit(joinPoint, null, SEARCH, userClaims, fields, null);
  }

  private Object prepareAndSendDbAudit(
      ProceedingJoinPoint joinPoint, String tableName, String action, JwtClaimsDto userClaims,
      Set<String> fields, String entityId) throws Throwable {

    String methodName = joinPoint.getSignature().getName();
    databaseEventsFacade
        .sendDbAudit(methodName, tableName, action, userClaims, BEFORE, entityId, fields, null);

    Object result = joinPoint.proceed();

    if (action.equals(CREATE)) {
      entityId = result.toString();
    }
    if (action.equals(READ)) {
      fields = getFields(((Optional) result).orElse(null));
    }
    databaseEventsFacade
        .sendDbAudit(methodName, tableName, action, userClaims, AFTER, entityId, fields, null);
    return result;
  }


  private Set<String> getFields(Object dto) {
    if (dto == null) {
      return null;
    }
    Set<String> fields = entityConverter.entityToMap(dto).keySet();
    if (fields.isEmpty()) {
      fields = null;
    }
    return fields;
  }
}