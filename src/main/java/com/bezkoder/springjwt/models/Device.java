package com.bezkoder.springjwt.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

import java.util.Date;

@Entity
@Table(name = "device")
@Data
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @UUID
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @NotBlank
    @Size(max = 120)
    private String deviceId;

    private Integer userId;

    @Setter
    private String username;

    private boolean shared;

    private boolean valid;

    private String secretId = "";

    private Date onboardingDate;

    private String deviceName;

    private int temp;

    private int idlet;

    private int maxwater;

    private int mode;

    private String pin;

    private int rg;

}
