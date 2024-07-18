package com.bezkoder.springjwt.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "user_device")
@Data
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userName;

    @NotBlank
    @Size(max = 120)
    private String deviceId;

    private Date createDate;

    private Date updateDate;
}
