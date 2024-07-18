package com.bezkoder.springjwt.payload.request;

public class ChangeTempuratureRequest {

    /**
     * temp type for the device
     */
    private String temp_type;

    /**
     * device_id
     */
    private String device_id;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getTemp_type() {
        return temp_type;
    }

    public void setTemp_type(String temp_type) {
        this.temp_type = temp_type;
    }
}
