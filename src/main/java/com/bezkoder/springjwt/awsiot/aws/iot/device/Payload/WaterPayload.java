package com.bezkoder.springjwt.awsiot.aws.iot.device.Payload;

import lombok.Data;

@Data
public class WaterPayload {


    private String temp;

    private String device_id;

}