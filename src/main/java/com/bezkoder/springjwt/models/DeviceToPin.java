package com.bezkoder.springjwt.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "device_pin")
@Data
public class DeviceToPin {
    @Id
    private String deviceId;

    private String pin;
}
