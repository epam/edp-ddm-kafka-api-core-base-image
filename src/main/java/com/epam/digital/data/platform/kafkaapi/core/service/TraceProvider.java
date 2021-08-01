package com.epam.digital.data.platform.kafkaapi.core.service;

public interface TraceProvider {

  String getRequestId();

  String getSourceSystem();

  String getSourceBusinessId();

  String getSourceBusinessProcess();
}
