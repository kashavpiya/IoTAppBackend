package com.bezkoder.springjwt.models;

import lombok.Data;

import java.util.Date;

@Data
public class Saving {

    private double waterSaved;

    private double electricitySaved;

    private double electricityCostSaved;

    private double CO2Saved;

    private long totalTime;

    private int totalCycles;

    private double coalSaved;

    private Date sampleDate;
}
