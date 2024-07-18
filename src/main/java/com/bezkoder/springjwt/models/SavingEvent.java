package com.bezkoder.springjwt.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "savings_data")
public class SavingEvent {

    @DynamoDBHashKey
    private Long userId;

    @DynamoDBRangeKey
    private Long sample_time;

    @DynamoDBAttribute
    private Saving payload;
}
