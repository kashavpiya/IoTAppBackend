package com.bezkoder.springjwt.payload.request;
import lombok.Data;
@Data
public class UserRequest {
    private Integer userId;

    private String username;
}
