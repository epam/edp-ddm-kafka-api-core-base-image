package com.epam.digital.data.platform.kafkaapi.core.util;

public class Header {

  private Header() {
  }

  public static final String X_SOURCE_SYSTEM = "x-source-system";
  public static final String X_SOURCE_BUSINESS_ID = "x-source-business-id";
  public static final String X_SOURCE_BUSINESS_PROCESS = "x-source-business-process";

  public static final String TRACE_ID = "X-B3-TraceId";
}
