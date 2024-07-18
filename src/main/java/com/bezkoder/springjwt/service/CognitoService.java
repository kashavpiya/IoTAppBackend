package com.bezkoder.springjwt.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import com.bezkoder.springjwt.models.AuthTokens;

import java.util.HashMap;
import java.util.Map;

public class CognitoService {
    private final CognitoIdentityProviderClient cognitoClient;
    private String refreshToken;

    public CognitoService() {
        // Initialize Cognito client
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public String authenticateUser(String username, String password, String secretHash) {
        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId("3uq79ioes8do9s35vtjnb4vfgr")
                .authFlow("USER_PASSWORD_AUTH")
                .authParameters(
                        Map.of(
                                "USERNAME", username,
                                "PASSWORD", password,
                                "SECRET_HASH", secretHash
                        )
                )
                .build();

        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType resultType = authResponse.authenticationResult();

        return resultType.accessToken();
    }

    public AuthTokens authenticateAndGenerateTokens(String username, String password, String secretHash) {
        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId("3uq79ioes8do9s35vtjnb4vfgr")
                .authFlow("USER_PASSWORD_AUTH")
                .authParameters(
                        Map.of(
                                "USERNAME", username,
                                "PASSWORD", password,
                                "SECRET_HASH", secretHash
                        )
                )
                .build();

        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType resultType = authResponse.authenticationResult();

        AuthTokens tokens = new AuthTokens();
        tokens.setAccessToken(resultType.accessToken());
        tokens.setRefreshToken(resultType.refreshToken());

        return tokens;
    }

    public String refreshToken(String refreshToken, String secretHash) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("REFRESH_TOKEN", refreshToken);
        authParams.put("SECRET_HASH", secretHash);

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId("3uq79ioes8do9s35vtjnb4vfgr")
                .authFlow("REFRESH_TOKEN_AUTH")
                .authParameters(authParams)
                .build();

        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType resultType = authResponse.authenticationResult();

        return resultType.accessToken();
    }

}
