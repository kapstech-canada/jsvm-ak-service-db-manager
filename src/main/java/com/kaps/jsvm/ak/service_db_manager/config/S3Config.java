package com.kaps.jsvm.ak.service_db_manager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3Config {
   private static final Logger log = LoggerFactory.getLogger(S3Config.class);
   @Value("${cloud.aws.credentials.accessKey}")
   private String accessKey;
   @Value("${cloud.aws.credentials.secretKey}")
   private String secretKey;
   @Value("${cloud.aws.credentials.region}")
   private String region;

   @Bean
   public S3Client s3Client() {
      log.info("Creating S3 Client " + this.accessKey);
      AwsBasicCredentials credentials = AwsBasicCredentials.create(this.accessKey, this.secretKey);
      return (S3Client)((S3ClientBuilder)((S3ClientBuilder)S3Client.builder().region(Region.of(this.region))).credentialsProvider(StaticCredentialsProvider.create(credentials))).build();
   }
}
