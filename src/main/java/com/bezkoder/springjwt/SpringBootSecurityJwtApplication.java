package com.bezkoder.springjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBootSecurityJwtApplication {

    public static void main(String[] args) {
        java.security.Security.setProperty("jdk.security.caDistrustPolicies", "");
        System.setProperty("javax.net.ssl.trustStrore", "C:/Program Files/Zulu/zulu-11/lib/security/cacert");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        SpringApplication.run(SpringBootSecurityJwtApplication.class, args);
    }

}
