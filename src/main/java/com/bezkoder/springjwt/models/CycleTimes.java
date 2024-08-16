package com.bezkoder.springjwt.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "CycleTimes")
public class CycleTimes {

    @DynamoDBHashKey(attributeName = "DeviceID")
    private String deviceID;

    @DynamoDBRangeKey(attributeName = "Timestamp")
    private Long timestamp;

    @DynamoDBAttribute(attributeName = "CycleTime")
    private Integer cycleTime;
}
