package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.payload.request.LoginRequest;
import com.bezkoder.springjwt.payload.response.JwtResponse;
import com.bezkoder.springjwt.security.jwt.JwtUtils;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import com.bezkoder.springjwt.service.CognitoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

import java.io.IOException;
import com.bezkoder.springjwt.config.CognitoUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    AuthenticationManager authenticationManager;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String secretHash = CognitoUtils.calculateSecretHash(loginRequest.getUsername(), "3uq79ioes8do9s35vtjnb4vfgr", "rcaib42uqirub26m891234igliv803i324op1ff8mtr8hp4hvq9");
            String accessToken = cognitoService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword(), secretHash);
            // Return the accessToken or handle the authentication result as needed
            return ResponseEntity.ok("User authenticated. Access Token: " + accessToken);

//            return ResponseEntity.ok(new JwtResponse(accessToken,
//                    userDetails.getId(),
//                    userDetails.getUsername(),
//                    userDetails.getEmail()));


        } catch (Exception e) {
            // Handle authentication failure
            System.out.print(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    @GetMapping("/login")
    public void redirectToOAuthLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String oauth2LoginEndpoint = "/oauth2/authorization/cognito";
        response.sendRedirect(oauth2LoginEndpoint);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> oauthCallback(
            @RequestParam(name = "access_token") String accessToken,
            HttpServletRequest request, HttpServletResponse response) {

        // Assuming you have a method to validate the access token with Cognito and get user details
        GetUserResponse getUserResponse = getUserDetailsFromAccessToken(accessToken);

        if (getUserResponse != null) {
            // Extract user details from the Cognito response
            String username = getUserResponse.username();
            String email = getUserResponse.userAttributes().stream()
                    .filter(attr -> attr.name().equals("email"))
                    .findFirst()
                    .map(attr -> attr.value())
                    .orElse(null);
            // You can extract other user attributes as needed

            return ResponseEntity.ok(new UserDetails(username, email));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OAuth login failed");
        }
    }


    private GetUserResponse getUserDetailsFromAccessToken(String accessToken) {
        try {
            GetUserRequest getUserRequest = GetUserRequest.builder()
                    .accessToken(accessToken)
                    .build();
            return cognitoClient.getUser(getUserRequest);
        } catch (CognitoIdentityProviderException e) {
            // Handle Cognito exception
            return null;
        }
    }


    // Class representing user details
    private static class UserDetails {
        private final String username;
        private final String email;

        public UserDetails(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }

}
