package com.epam.digital.data.platform.kafkaapi.core.audit;

import com.epam.digital.data.platform.kafkaapi.core.util.Header;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcAuditSourceInfoProvider implements AuditSourceInfoProvider {

  @Override
  public AuditSourceInfo getAuditSourceInfo() {
    return AuditSourceInfo.AuditSourceInfoBuilder.anAuditSourceInfo()
        .system(MDC.get(Header.X_SOURCE_SYSTEM))
        .application(MDC.get(Header.X_SOURCE_APPLICATION))
        .businessProcess(MDC.get(Header.X_SOURCE_BUSINESS_PROCESS))
        .businessProcessDefinitionId(MDC.get(Header.X_SOURCE_BUSINESS_PROCESS_DEFINITION_ID))
        .businessProcessInstanceId(MDC.get(Header.X_SOURCE_BUSINESS_PROCESS_INSTANCE_ID))
        .businessActivity(MDC.get(Header.X_SOURCE_BUSINESS_ACTIVITY))
        .businessActivityInstanceId(MDC.get(Header.X_SOURCE_BUSINESS_ACTIVITY_INSTANCE_ID))
        .build();
  }
}
