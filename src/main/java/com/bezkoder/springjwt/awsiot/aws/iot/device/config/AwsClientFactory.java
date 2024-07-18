package com.bezkoder.springjwt.awsiot.aws.iot.device.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClientBuilder;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;

@Configuration
public class AwsClientFactory {

    @Bean
    public AWSIot getIotClient(AwsIotAccountConfig appConfig) {
        return AWSIotClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider
                        (new BasicAWSCredentials(appConfig.getAccessKeyId(), appConfig.getSecretAccessKey())))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    @Bean
    public AWSIotDataClient getIotDataClient(final AwsIotAccountConfig appConfig) {

        return (AWSIotDataClient) AWSIotDataClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {

                    public String getAWSSecretKey() {
                        return appConfig.getSecretAccessKey();

                    }

                    public String getAWSAccessKeyId() {
                        return appConfig.getAccessKeyId();
                    }
                }))
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}