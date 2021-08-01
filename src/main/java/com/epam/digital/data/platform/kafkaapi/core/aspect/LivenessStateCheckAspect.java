package com.epam.digital.data.platform.kafkaapi.core.aspect;

import com.epam.digital.data.platform.model.core.kafka.Response;
import com.epam.digital.data.platform.model.core.kafka.Status;
import com.epam.digital.data.platform.starter.actuator.livenessprobe.LivenessStateHandler;
import java.util.EnumSet;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LivenessStateCheckAspect implements KafkaResponseListenerAspect {

  private static final EnumSet<Status> LIVENESS_UNHEALTHY_STATUSES = EnumSet.of(
      Status.THIRD_PARTY_SERVICE_UNAVAILABLE, Status.INVALID_SIGNATURE,
      Status.INTERNAL_CONTRACT_VIOLATION, Status.PROCEDURE_ERROR);

  private final LivenessStateHandler livenessStateHandler;

  public LivenessStateCheckAspect(LivenessStateHandler livenessStateHandler) {
    this.livenessStateHandler = livenessStateHandler;
  }

  @Override
  public void handleKafkaResponse(Message<? extends Response> response) {
    livenessStateHandler
        .handleResponse(response.getPayload().getStatus(), LIVENESS_UNHEALTHY_STATUSES::contains);
  }
}
