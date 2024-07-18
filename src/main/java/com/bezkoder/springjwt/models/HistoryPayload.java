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
public class HistoryPayload {

    @DynamoDBAttribute
    private int shower_time;

    @DynamoDBAttribute
    private int cycles;
}


