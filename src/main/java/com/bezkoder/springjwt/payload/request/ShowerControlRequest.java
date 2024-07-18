package com.bezkoder.springjwt.payload.request;

import lombok.Data;

@Data
public class ShowerControlRequest {
    private String device_id;
    private String command;
}
