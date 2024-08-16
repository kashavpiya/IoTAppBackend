package com.bezkoder.springjwt.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBDocument
@NoArgsConstructor
@AllArgsConstructor
public class Payload {
    @DynamoDBAttribute(attributeName = "ID")
    private String deviceId;
    @DynamoDBAttribute
    private double temp;
    @DynamoDBAttribute
    private int status;
    @DynamoDBAttribute
    private long batt;
    @DynamoDBAttribute
    private double voltage;
    @DynamoDBAttribute
    private double distance;
    @DynamoDBAttribute
    private long cycleTime;
    @DynamoDBAttribute
    private long totalTime;
    @DynamoDBAttribute
    private int cycles;
    @DynamoDBAttribute(attributeName = "Sleep")
    private long sleep;
    @DynamoDBAttribute
    private String version;

}
