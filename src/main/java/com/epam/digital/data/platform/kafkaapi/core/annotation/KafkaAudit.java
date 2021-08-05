package com.epam.digital.data.platform.kafkaapi.core.annotation;

import com.epam.digital.data.platform.kafkaapi.core.util.Operation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KafkaAudit {

  Operation value();
}
