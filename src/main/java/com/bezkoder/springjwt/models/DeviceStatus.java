package com.bezkoder.springjwt.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class DeviceStatus {
    @JsonProperty("DeviceID")
    private String deviceID;
    @JsonProperty("Temp")
    private int temp;
    @JsonProperty("Distance")
    private double distance;
    private int touch_value;
    @JsonProperty("Timer")
    private int timer;
    @JsonProperty("SetTemp")
    private int setTemp;
    private int touchBenchValue;
    @JsonProperty("Status")
    private int status;
    @JsonProperty("V")
    private double v;
    @JsonProperty("CurrentOnTime")
    private int currentOnTime;
    @JsonProperty("Cycles")
    private int cycles;
    @JsonProperty("TC")
    private int tC;
    @JsonProperty("Command")
    private int command;
    @JsonProperty("Sleep")
    private int Sleep;
}
