package com.bezkoder.springjwt.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "ShowerEventsData")
public class ShowerEvent {

    @DynamoDBHashKey
    private String device_id;

    @DynamoDBRangeKey
    private Long sample_time;

    @DynamoDBAttribute
    private HistoryPayload payload;
}
