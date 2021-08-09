package com.epam.digital.data.platform.kafkaapi.core.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.integration.ceph.service.impl.CephServiceS3Impl;
import com.epam.digital.data.platform.kafkaapi.core.service.KeycloakRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {DigitalSealRestClient.class, KeycloakRestClient.class})
public class ThirdPartySystemsConfig {

  @Bean
  public CephService datafactoryCephService(
      @Value("${ceph.http-endpoint}") String uri,
      @Value("${ceph.access-key}") String accessKey,
      @Value("${ceph.secret-key}") String secretKey) {
    return new CephServiceS3Impl(cephAmazonS3(uri, accessKey, secretKey));
  }

  @Bean
  public CephService datafactoryResponseCephService(
          @Value("${datafactory-response-ceph.http-endpoint}") String uri,
          @Value("${datafactory-response-ceph.access-key}") String accessKey,
          @Value("${datafactory-response-ceph.secret-key}") String secretKey) {
    return new CephServiceS3Impl(cephAmazonS3(uri, accessKey, secretKey));
  }

  @Bean
  public CephService datafactoryFileCephService(
          @Value("${datafactory-file-ceph.http-endpoint}") String uri,
          @Value("${datafactory-file-ceph.access-key}") String accessKey,
          @Value("${datafactory-file-ceph.secret-key}") String secretKey) {
    return new CephServiceS3Impl(cephAmazonS3(uri, accessKey, secretKey));
  }

  private AmazonS3 cephAmazonS3(String uri, String accessKey, String secretKey) {

    var credentials =
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));

    var clientConfig = new ClientConfiguration();
    clientConfig.setProtocol(Protocol.HTTP);

    return AmazonS3ClientBuilder.standard()
        .withCredentials(credentials)
        .withClientConfiguration(clientConfig)
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(uri, null))
        .withPathStyleAccessEnabled(true)
        .build();
  }
}