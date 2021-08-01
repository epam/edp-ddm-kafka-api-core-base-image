package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.kafkaapi.core.config.KeycloakConfigProperties;
import com.epam.digital.data.platform.kafkaapi.core.exception.ExternalCommunicationException;
import com.epam.digital.data.platform.kafkaapi.core.exception.JwtExpiredException;
import com.epam.digital.data.platform.kafkaapi.core.exception.JwtValidationException;
import com.epam.digital.data.platform.model.core.kafka.Request;
import com.epam.digital.data.platform.model.core.kafka.SecurityContext;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.PublishedRealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtValidationService {

  private final Logger log = LoggerFactory.getLogger(JwtValidationService.class);

  private final boolean jwtValidationEnabled;
  private final KeycloakConfigProperties keycloakConfigProperties;

  private final KeycloakRestClient keycloakRestClient;
  private final Clock clock;

  public JwtValidationService(
      @Value("${data-platform.jwt.validation.enabled:false}") boolean jwtValidationEnabled,
      KeycloakConfigProperties keycloakConfigProperties,
      KeycloakRestClient keycloakRestClient, Clock clock) {
    this.jwtValidationEnabled = jwtValidationEnabled;
    this.keycloakConfigProperties = keycloakConfigProperties;
    this.keycloakRestClient = keycloakRestClient;
    this.clock = clock;
  }

  public <O> boolean isValid(Request<O> input) {
    if (!jwtValidationEnabled) {
      return true;
    }
    String accessToken = getTokenFromInput(input);
    JWTClaimsSet jwtClaimsSet = getClaimsFromToken(accessToken);
    if (isExpiredJwt(jwtClaimsSet)) {
      throw new JwtExpiredException("JWT is expired");
    }
    String jwtIssuer = jwtClaimsSet.getIssuer();
    String issuerRealm = jwtIssuer.substring(jwtIssuer.lastIndexOf("/") + 1);
    if (keycloakConfigProperties.getRealms().contains(issuerRealm)) {
      PublicKey keycloakPublicKey = getPublicKeyFromKeycloak(issuerRealm);
      return isVerifiedToken(accessToken, keycloakPublicKey);
    } else {
      throw new JwtValidationException("Issuer realm is not valid");
    }
  }

  private JWTClaimsSet getClaimsFromToken(String accessToken) {
    try {
      return JWTParser.parse(accessToken)
          .getJWTClaimsSet();
    } catch (ParseException e) {
      throw new JwtValidationException("Error while JWT parsing", e);
    }
  }

  private <O> String getTokenFromInput(Request<O> input) {
    return Optional.ofNullable(input.getSecurityContext())
        .map(SecurityContext::getAccessToken)
        .orElse("");
  }

  private boolean isExpiredJwt(JWTClaimsSet jwtClaimsSet) {
    Date now = new Date(clock.millis());
    return Optional.of(jwtClaimsSet.getExpirationTime())
        .map(now::after)
        .orElse(true);
  }

  private PublicKey getPublicKeyFromKeycloak(String realm) {
    try {
      PublishedRealmRepresentation realmRepresentation = keycloakRestClient
          .getRealmRepresentation(realm);
      return realmRepresentation.getPublicKey();
    } catch (Exception e) {
      throw new ExternalCommunicationException("Cannot get public key from keycloak",
          e, Status.THIRD_PARTY_SERVICE_UNAVAILABLE);
    }
  }

  private boolean isVerifiedToken(String accessToken, PublicKey publicKey) {
    try {
      TokenVerifier.create(accessToken, JsonWebToken.class)
          .publicKey(publicKey)
          .verify();
      return true;
    } catch (VerificationException e) {
      log.error("JWT token is not verified", e);
      return false;
    }
  }
}
