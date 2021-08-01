package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.kafkaapi.core.config.KeycloakConfigProperties;
import com.epam.digital.data.platform.kafkaapi.core.exception.ExternalCommunicationException;
import com.epam.digital.data.platform.kafkaapi.core.exception.JwtExpiredException;
import com.epam.digital.data.platform.kafkaapi.core.exception.JwtValidationException;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.SecurityContext;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.PublishedRealmRepresentation;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JwtValidationServiceTest {

  private static final String REALM = "realm";

  @MockBean
  private KeycloakConfigProperties keycloakConfigProperties;
  @MockBean
  private KeycloakRestClient keycloakRestClient;
  @MockBean
  private Clock clock;

  private JwtValidationService jwtValidationService;

  @BeforeEach
  void beforeEach() {
    jwtValidationService = new JwtValidationService(true, keycloakConfigProperties,
            keycloakRestClient, clock);

    when(keycloakConfigProperties.getRealms()).thenReturn(Collections.singletonList(REALM));
    when(clock.millis())
        .thenReturn(LocalDateTime.of(2021, 3, 1, 11, 50)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
  }

  @Test
  void expectOperationTokenVerifiedWhenProcessingDisabled() throws JOSEException {
    jwtValidationService = new JwtValidationService(false, keycloakConfigProperties,
            keycloakRestClient, clock);
    Request<Void> input = mockRequest("", new Date());

    boolean actual = jwtValidationService.isValid(input);

    assertThat(actual).isTrue();
  }

  @Test
  void expectExceptionWhenOperationWithNoToken() {
    jwtValidationService = new JwtValidationService(true, keycloakConfigProperties,
            keycloakRestClient, clock);
    Request<Void> input = new Request<>();

    JwtValidationException e =
        assertThrows(
            JwtValidationException.class, () -> jwtValidationService.isValid(input));
    assertThat(e.getKafkaResponseStatus()).isEqualTo(Status.JWT_INVALID);
    assertThat(e.getDetails()).isNull();
  }

  @Test
  void expectTokenNonVerifiedWhenInvalidPublicKeyReturned() throws JOSEException {
    when(keycloakRestClient.getRealmRepresentation(REALM))
        .thenReturn(new PublishedRealmRepresentation());
    Date tokenExp = Date.from(LocalDateTime.of(2021, 3, 1, 12, 0)
        .atZone(ZoneId.systemDefault()).toInstant());
    Request<Void> input = mockRequest("/" + REALM,  tokenExp);

    boolean actual = jwtValidationService.isValid(input);

    assertThat(actual).isFalse();
  }

  @Test
  void expectThirdPartyUnavailableWhenRestCallException() throws JOSEException {
    when(keycloakRestClient.getRealmRepresentation(REALM))
        .thenThrow(new RuntimeException());

    Date tokenExp = Date.from(LocalDateTime.of(2021, 3, 1, 12, 0)
        .atZone(ZoneId.systemDefault()).toInstant());
    Request<Void> input = mockRequest("/" + REALM, tokenExp);

    ExternalCommunicationException e = assertThrows(ExternalCommunicationException.class,
        () -> jwtValidationService.isValid(input));

    assertThat(e.getKafkaResponseStatus()).isEqualTo(Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
  }

  @Test
  void expectJwtVerificationExceptionWhenIssuerRealmIncorrect() throws JOSEException {

    Date tokenExp = Date.from(LocalDateTime.of(2021, 3, 1, 12, 0)
            .atZone(ZoneId.systemDefault()).toInstant());
    Request<Void> input = mockRequest("/wrongRealm", tokenExp);

    JwtValidationException e = assertThrows(JwtValidationException.class,
            () -> jwtValidationService.isValid(input));

    assertThat(e.getKafkaResponseStatus()).isEqualTo(Status.JWT_INVALID);
  }

  @Test
  void expectTokenExpiredWhenExpDateAfterCurrent() throws JOSEException {
    Date tokenExp = Date.from(LocalDateTime.of(2021, 3, 1, 11, 45)
        .atZone(ZoneId.systemDefault()).toInstant());
    Request<Void> input = mockRequest("", tokenExp);

    JwtExpiredException e = assertThrows(JwtExpiredException.class, () -> jwtValidationService.isValid(input));
    assertThat(e.getKafkaResponseStatus()).isEqualTo(Status.JWT_EXPIRED);
    assertThat(e.getDetails()).isNull();
  }

  private Request<Void> mockRequest(String jwtIssuer, Date jwtExpirationTime) throws JOSEException {
    var request = new Request<Void>();
    var securityContext = new SecurityContext();
    securityContext.setAccessToken(mockJwt(jwtIssuer, jwtExpirationTime));
    request.setSecurityContext(securityContext);
    return request;
  }

  private String mockJwt(String issuer, Date expirationTime) throws JOSEException {
    ECKey key = new ECKeyGenerator(Curve.P_521)
        .keyID("123")
        .generate();
    JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES512)
        .type(JOSEObjectType.JWT)
        .keyID(key.getKeyID())
        .build();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .expirationTime(expirationTime)
            .issuer(issuer)
        .build();
    SignedJWT signedJWT = new SignedJWT(header, claims);
    signedJWT.sign(new ECDSASigner(key.toECPrivateKey()));
    return signedJWT.serialize();
  }
}