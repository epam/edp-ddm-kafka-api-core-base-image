package com.epam.digital.data.platform.kafkaapi.core.aspect;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.service.DatabaseEventsFacade;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditDatabaseEventsAspect implements DbUpdateAspect {

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
  private final EntityConverter entityConverter;

  public AuditDatabaseEventsAspect(DatabaseEventsFacade databaseEventsFacade,
      JwtInfoProvider jwtInfoProvider, EntityConverter entityConverter) {
    this.databaseEventsFacade = databaseEventsFacade;
    this.jwtInfoProvider = jwtInfoProvider;
    this.entityConverter = entityConverter;
  }

  @Around("(findById()) && args(request)")
  Object auditFindById(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
    JwtClaimsDto userClaims = jwtInfoProvider.getUserClaims(request);
    String entityId = request.getPayload().toString();
    return prepareAndSendDbAudit(joinPoint, null, READ, userClaims, null, entityId);
  }

  @Around("(save()) && args(tableName, userClaims, sysValues, businessValues)")
  Object auditInsert(ProceedingJoinPoint joinPoint, String tableName, JwtClaimsDto userClaims,
      Map<String, String> sysValues, Map<String, Object> businessValues) throws Throwable {

    return prepareAndSendDbAudit(joinPoint, tableName, CREATE, userClaims, businessValues.keySet(),
        null);
  }

  @Around("(update()) && args(tableName, userClaims, entityId, sysValues, businessValues)")
  Object auditUpdate(
      ProceedingJoinPoint joinPoint, String tableName, JwtClaimsDto userClaims, String entityId,
      Map<String, String> sysValues, Map<String, Object> businessValues) throws Throwable {

    return prepareAndSendDbAudit(joinPoint, tableName, UPDATE, userClaims, businessValues.keySet(),
        entityId);
  }

  @Around("(delete()) && args(tableName, userClaims, entityId, sysValues)")
  Object auditDelete(
      ProceedingJoinPoint joinPoint, String tableName, JwtClaimsDto userClaims, String entityId,
      Map<String, String> sysValues) throws Throwable {

    return prepareAndSendDbAudit(joinPoint, tableName, DELETE, userClaims, null, entityId);
  }

  @Around("(search()) && args(request)")
  Object auditSearch(ProceedingJoinPoint joinPoint, Request<?> request) throws Throwable {
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
      fields = getFields(((Optional)result).orElse(null));
    }
    databaseEventsFacade
        .sendDbAudit(methodName, tableName, action, userClaims, AFTER, entityId, fields, null);
    return result;
  }

  private Set<String> getFields(Object dto) {
    if(dto == null) {
      return null;
    }
    Set<String> fields = entityConverter.entityToMap(dto).keySet();
    if(fields.isEmpty()) {
      fields = null;
    }
    return fields;
  }
}
