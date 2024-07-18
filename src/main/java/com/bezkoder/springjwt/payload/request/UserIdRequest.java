package com.bezkoder.springjwt.payload.request;

import lombok.Data;
@Data
public class UserIdRequest {

    private Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }



}
