package com.bezkoder.springjwt.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class RefreshTokenRequest {

    private String refreshToken;

    private String username;

    private Long id;

    private String email;

}
