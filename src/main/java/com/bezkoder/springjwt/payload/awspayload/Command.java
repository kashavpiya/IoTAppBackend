package com.bezkoder.springjwt.payload.awspayload;

import lombok.Data;

@Data
public class Command {

    private String cmd;

    private int temp;

    private int idlet;

    private int maxwater;

    private int md;

    private int rg;

    @Override
    public String toString() {
        return "Command{" +
                "cmd='" + cmd + '\'' +
                ", temp=" + temp +
                ", idlet=" + idlet +
                ", maxwater=" + maxwater +
                ", mode=" + md +
                ", rg=" + rg +
                '}';
    }
}
