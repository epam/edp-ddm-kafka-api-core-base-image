package com.epam.digital.data.platform.kafkaapi.core.commandhandler;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.kafkaapi.core.commandhandler.impl.CommandHandlerTestImpl;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.model.DmlOperationArgs;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.DmlOperationHandler;
import com.epam.digital.data.platform.kafkaapi.core.commandhandler.util.EntityConverter;
import com.epam.digital.data.platform.kafkaapi.core.exception.ConstraintViolationException;
import com.epam.digital.data.platform.kafkaapi.core.service.JwtInfoProvider;
import com.epam.digital.data.platform.kafkaapi.core.util.MockEntity;
import com.epam.digital.data.platform.model.core.kafka.EntityId;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.RequestContext;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import com.epam.digital.data.platform.starter.security.dto.RolesDto;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = CommandHandlerTestImpl.class)
class AbstractCommandHandlerTest {

  private static final String USER_ID = "user";
  private static final String ROLE = "role";

  private static final UUID ENTITY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
  private Request<MockEntity> request;

  @MockBean
  private EntityConverter<MockEntity> entityConverter;
  @MockBean
  private DmlOperationHandler dmlOperationHandler;
  @MockBean
  private JwtInfoProvider jwtInfoProvider;

  @Autowired
  private CommandHandlerTestImpl commandHandler;

  private final JwtClaimsDto userClaims = getMockedClaims();

  @BeforeEach
  void setUp() {
    request = new Request<>(getMockedFactor(), new RequestContext(), null);
    when(jwtInfoProvider.getUserClaims(request)).thenReturn(userClaims);
  }

  @Test
  void expectSaveOperationWithPreparedParamsCalled() {
    Map<String, Object> mockEntityMap = getMockedEntityMap();
    when(entityConverter.entityToMap(any())).thenReturn(mockEntityMap);
    Map<String, String> mockSysValuesMap = new HashMap<>();
    when(entityConverter.buildSysValues(USER_ID, request)).thenReturn(mockSysValuesMap);
    when(dmlOperationHandler.save(
        DmlOperationArgs.builder("table", userClaims, mockSysValuesMap)
            .saveOperationArgs(mockEntityMap).build()))
        .thenReturn(ENTITY_ID.toString());

    var result = commandHandler.save(request);

    verify(entityConverter).entityToMap(request.getPayload());
    verify(entityConverter).buildSysValues(USER_ID, request);
    verify(dmlOperationHandler)
        .save(
            DmlOperationArgs.builder("table", userClaims, mockSysValuesMap)
                .saveOperationArgs(mockEntityMap)
                .build());

    assertThat(mockEntityMap).doesNotContainKey("consent_id");
    assertThat(result).isEqualTo(new EntityId(ENTITY_ID));
  }

  @Test
  void expectUpdateOperationWithPreparedParamsCalled() {
    Map<String, Object> mockEntityMap = getMockedEntityMap();
    when(entityConverter.entityToMap(any())).thenReturn(mockEntityMap);
    Map<String, String> mockSysValuesMap = new HashMap<>();
    when(entityConverter.buildSysValues(USER_ID, request)).thenReturn(mockSysValuesMap);

    commandHandler.update(request);

    verify(entityConverter).entityToMap(request.getPayload());
    verify(entityConverter).buildSysValues(USER_ID, request);
    verify(dmlOperationHandler)
        .update(
            DmlOperationArgs.builder("table", userClaims, mockSysValuesMap)
                .updateOperationArgs(ENTITY_ID.toString(), mockEntityMap)
                .build());

    assertThat(mockEntityMap).doesNotContainKey("consent_id");
  }

  @Test
  void expectExceptionWhenUpdateEntityWithoutId() {
    Map<String, Object> mockEntityMap = new HashMap<>();
    mockEntityMap.put("consent_id", null);
    mockEntityMap.put("person_full_name", "stub");
    when(entityConverter.entityToMap(any())).thenReturn(mockEntityMap);

    ConstraintViolationException e =
        assertThrows(ConstraintViolationException.class, () -> commandHandler.update(request));

    assertThat(e.getKafkaResponseStatus()).isEqualTo(Status.CONSTRAINT_VIOLATION);
    assertThat(e.getDetails()).isEqualTo("not null");
  }

  @Test
  void expectDeleteOperationWithPreparedParamsCalled() {
    when(entityConverter.getUuidOfEntity(any(), any())).thenReturn(ENTITY_ID.toString());
    Map<String, String> mockSysValuesMap = new HashMap<>();
    when(entityConverter.buildSysValues(USER_ID, request)).thenReturn(mockSysValuesMap);

    commandHandler.delete(request);

    verify(entityConverter).getUuidOfEntity(request.getPayload(), "consent_id");
    verify(entityConverter).buildSysValues(USER_ID, request);
    verify(dmlOperationHandler)
        .delete(
            DmlOperationArgs.builder("table", userClaims, mockSysValuesMap)
                .deleteOperationArgs(ENTITY_ID.toString())
                .build());
  }

  private JwtClaimsDto getMockedClaims() {
    JwtClaimsDto userClaims = new JwtClaimsDto();
    userClaims.setDrfo(USER_ID);
    RolesDto rolesDto = new RolesDto();
    rolesDto.setRoles(singletonList(ROLE));
    userClaims.setRealmAccess(rolesDto);
    return userClaims;
  }

  private MockEntity getMockedFactor() {
    MockEntity dto = new MockEntity();
    dto.setConsentId(ENTITY_ID);
    dto.setPersonFullName("name");
    return dto;
  }

  private Map<String, Object> getMockedEntityMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("consent_id", ENTITY_ID);
    map.put("person_full_name", "stub");
    return map;
  }
}
