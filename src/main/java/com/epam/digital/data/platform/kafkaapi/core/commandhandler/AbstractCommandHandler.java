package com.epam.digital.data.platform.kafkaapi.core.commandhandler;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.model.DmlOperationArgs;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.exception.ConstraintViolationException;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.model.core.kafka.EntityId;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractCommandHandler<T> implements CommandHandler<T> {

  @Autowired
  private JwtInfoProvider jwtInfoProvider;
  @Autowired
  private DmlOperationHandler dmlOperationHandler;

  private final EntityConverter<T> entityConverter;

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected AbstractCommandHandler(
      EntityConverter<T> entityConverter) {
    this.entityConverter = entityConverter;
  }

  @Override
  public EntityId save(Request<T> input) {
    JwtClaimsDto userClaims = jwtInfoProvider.getUserClaims(input);
    Map<String, Object> entityMap = entityConverter.entityToMap(input.getPayload());
    entityMap.remove(pkColumnName());
    Map<String, String> sysValues = entityConverter.buildSysValues(userClaims.getDrfo(), input);
    String id = dmlOperationHandler.save(
        DmlOperationArgs.builder(tableName(), userClaims, sysValues)
            .saveOperationArgs(entityMap)
            .build());
    return new EntityId(UUID.fromString(id));
  }

  @Override
  public void update(Request<T> input) {
    JwtClaimsDto userClaims = jwtInfoProvider.getUserClaims(input);
    Map<String, Object> entityMap = entityConverter.entityToMap(input.getPayload());
    Object entityId = entityMap.remove(pkColumnName());

    if (entityId == null) {
      log.error("No entity ID for update");
      throw new ConstraintViolationException("No entity ID for update", "not null");
    }

    Map<String, String> sysValues = entityConverter.buildSysValues(userClaims.getDrfo(), input);
    dmlOperationHandler.update(
        DmlOperationArgs.builder(tableName(), userClaims, sysValues)
            .updateOperationArgs(entityId.toString(), entityMap)
            .build());
  }

  @Override
  public void delete(Request<T> input) {
    JwtClaimsDto userClaims = jwtInfoProvider.getUserClaims(input);
    String entityId = entityConverter.getUuidOfEntity(input.getPayload(), pkColumnName());
    Map<String, String> sysValues = entityConverter.buildSysValues(userClaims.getDrfo(), input);
    dmlOperationHandler.delete(
        DmlOperationArgs.builder(tableName(), userClaims, sysValues)
            .deleteOperationArgs(entityId)
            .build());
  }

  public abstract String tableName();

  public abstract String pkColumnName();
}
