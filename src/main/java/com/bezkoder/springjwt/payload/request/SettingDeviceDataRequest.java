package com.bezkoder.springjwt.payload.request;

import lombok.Data;

@Data
public class SettingDeviceDataRequest {

    private  String device_id;

    private int temp;

    private int idlet;

    private int maxwater;

    private int rg;

    private int mode;

    private int userId;

    private String username;
}
