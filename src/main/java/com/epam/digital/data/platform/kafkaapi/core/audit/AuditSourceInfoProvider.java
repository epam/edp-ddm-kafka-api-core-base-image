package com.epam.digital.data.platform.kafkaapi.core.audit;

import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;

public interface AuditSourceInfoProvider {
    AuditSourceInfo getAuditSourceInfo();
}
