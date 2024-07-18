package com.bezkoder.springjwt.awsiot.aws.iot.device.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@ToString
@Configuration
public class AwsIotAccountConfig {

    /**
     * aws endpoint
     */
    @Value(value = "${aws.iot.clientEndpoint}")
    private String clientEndpoint;

    /**
     * client ID
     */
    @Value(value = "${aws.iot.clientId}")
    private String clientId;

    /**
     * access key
     */
    @Value(value = "${aws.iot.accessKeyId}")
    private String accessKeyId;

    /**
     * private key
     */
    @Value(value = "${aws.iot.secretAccessKey}")
    private String secretAccessKey;
}
