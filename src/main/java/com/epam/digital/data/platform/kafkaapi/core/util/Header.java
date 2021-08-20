package com.epam.digital.data.platform.kafkaapi.core.util;

public class Header {

  private Header() {
  }

  public static final String X_SOURCE_SYSTEM = "x-source-system";
  public static final String X_SOURCE_APPLICATION = "x-source-application";
  public static final String X_SOURCE_BUSINESS_PROCESS = "x-source-business-process";
  public static final String X_SOURCE_BUSINESS_PROCESS_INSTANCE_ID =
      "x-source-business-process-instance-id";
  public static final String X_SOURCE_BUSINESS_PROCESS_DEFINITION_ID =
      "x-source-business-process-definition-id";
  public static final String X_SOURCE_BUSINESS_ACTIVITY = "x-source-business-activity";
  public static final String X_SOURCE_BUSINESS_ACTIVITY_INSTANCE_ID =
      "x-source-business-activity-instance-id";

  public static final String TRACE_ID = "X-B3-TraceId";
}
