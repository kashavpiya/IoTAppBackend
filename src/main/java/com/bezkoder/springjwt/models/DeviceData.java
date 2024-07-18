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
@DynamoDBTable(tableName = "device_temp_monitoring")
public class DeviceData {
    @DynamoDBHashKey
    private String device_id;

    @DynamoDBRangeKey
    private Long sample_time;

    @DynamoDBAttribute(attributeName = "payload")
    private Payload payload;
}
