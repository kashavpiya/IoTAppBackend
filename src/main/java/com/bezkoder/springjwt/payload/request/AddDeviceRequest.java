package com.bezkoder.springjwt.payload.request;

import lombok.Data;

@Data
public class AddDeviceRequest {

    private String deviceId;

    private boolean shared;

    private boolean valid;

    private String pin;

    private String device_name;

    private Integer userId;
}
