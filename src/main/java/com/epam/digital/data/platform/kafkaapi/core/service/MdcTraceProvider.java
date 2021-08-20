package com.epam.digital.data.platform.kafkaapi.core.service;

import com.epam.digital.data.platform.kafkaapi.core.util.Header;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcTraceProvider implements TraceProvider {

  @Override
  public String getRequestId() {
    return MDC.get(Header.TRACE_ID);
  }
}
