/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.kafkaapi.core.config;

import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.integration.ceph.config.S3ConfigProperties;
import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.kafkaapi.core.service.KeycloakRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {DigitalSealRestClient.class, KeycloakRestClient.class})
public class ThirdPartySystemsConfig {

  @Bean
  @ConfigurationProperties(prefix = "s3.config")
  public S3ConfigProperties s3ConfigProperties() {
    return new S3ConfigProperties();
  }

  @Bean
  public CephS3Factory cephS3Factory() {
    return new CephS3Factory(s3ConfigProperties());
  }

  @Bean
  public CephService datafactoryCephService(
      @Value("${ceph.http-endpoint}") String uri,
      @Value("${ceph.access-key}") String accessKey,
      @Value("${ceph.secret-key}") String secretKey,
      CephS3Factory cephS3Factory) {
    return cephS3Factory.createCephService(uri, accessKey, secretKey);
  }

  @Bean
  public CephService datafactoryResponseCephService(
      @Value("${datafactory-response-ceph.http-endpoint}") String uri,
      @Value("${datafactory-response-ceph.access-key}") String accessKey,
      @Value("${datafactory-response-ceph.secret-key}") String secretKey,
      CephS3Factory cephS3Factory) {
    return cephS3Factory.createCephService(uri, accessKey, secretKey);
  }

  @Bean
  public CephService datafactoryFileCephService(
      @Value("${datafactory-file-ceph.http-endpoint}") String uri,
      @Value("${datafactory-file-ceph.access-key}") String accessKey,
      @Value("${datafactory-file-ceph.secret-key}") String secretKey,
      CephS3Factory cephS3Factory) {
    return cephS3Factory.createCephService(uri, accessKey, secretKey);
  }
}
