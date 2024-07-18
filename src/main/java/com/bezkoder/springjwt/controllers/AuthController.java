package com.bezkoder.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.bezkoder.springjwt.config.CognitoUtils;
import com.bezkoder.springjwt.models.AuthTokens;
import com.bezkoder.springjwt.payload.request.RefreshTokenRequest;
import com.bezkoder.springjwt.payload.request.ResetPasswordRequest;
import com.bezkoder.springjwt.service.CognitoService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.springjwt.models.ERole;
import com.bezkoder.springjwt.models.Role;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.request.LoginRequest;
import com.bezkoder.springjwt.payload.request.SignupRequest;
import com.bezkoder.springjwt.payload.response.JwtResponse;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.RoleRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.security.jwt.JwtUtils;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws Exception {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        String secretHash = CognitoUtils.calculateSecretHash(loginRequest.getUsername(), "3uq79ioes8do9s35vtjnb4vfgr", "rcaib42uqirub26m891234igliv803i324op1ff8mtr8hp4hvq9");
        AuthTokens tokens = cognitoService.authenticateAndGenerateTokens(loginRequest.getUsername(), loginRequest.getPassword(), secretHash);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken();

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(accessToken,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        String secretHash = CognitoUtils.calculateSecretHash(signUpRequest.getUsername(), "3uq79ioes8do9s35vtjnb4vfgr", "rcaib42uqirub26m891234igliv803i324op1ff8mtr8hp4hvq9");

        // Create new user's account in AWS Cognito
        AttributeType usernameAttribute = AttributeType.builder()
                .name("preferred_username")
                .value(signUpRequest.getUsername())
                .build();

        AttributeType emailAttribute = AttributeType.builder()
                .name("email")
                .value(signUpRequest.getEmail())
                .build();

        SignUpRequest cognitoSignUpRequest = SignUpRequest.builder()
                .clientId("3uq79ioes8do9s35vtjnb4vfgr")
                .username(signUpRequest.getUsername())
                .password(signUpRequest.getPassword())
                .userAttributes(usernameAttribute, emailAttribute)
                .secretHash(secretHash)
                .build();

        try {
            SignUpResponse signUpResponse = cognitoClient.signUp(cognitoSignUpRequest);
            // Handle successful Cognito signup response if needed
        } catch (CognitoIdentityProviderException e) {
            // Handle Cognito signup error
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.awsErrorDetails().errorMessage()));
        }

        // Create new user's account in your local UserRepository
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));


    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            String secretHash = CognitoUtils.calculateSecretHash(refreshTokenRequest.getUsername(), "3uq79ioes8do9s35vtjnb4vfgr", "rcaib42uqirub26m891234igliv803i324op1ff8mtr8hp4hvq9");

            // Refresh the access token using the refresh token
            String accessToken = cognitoService.refreshToken(refreshToken, secretHash); // Implement this method in your cognitoService to refresh the token

            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken, refreshTokenRequest.getId(), refreshTokenRequest.getUsername(), refreshTokenRequest.getEmail(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error refreshing token: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody com.bezkoder.springjwt.payload.request.ForgotPasswordRequest forgotPasswordRequest) {
        try {
            // Initiate the forgot password process in AWS Cognito
            software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest cognitoForgotPasswordRequest = software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest.builder()
                    .clientId("3uq79ioes8do9s35vtjnb4vfgr")
                    .username(forgotPasswordRequest.getUsername())
                    .secretHash(CognitoUtils.calculateSecretHash(forgotPasswordRequest.getUsername(), "3uq79ioes8do9s35vtjnb4vfgr", "rcaib42uqirub26m891234igliv803i324op1ff8mtr8hp4hvq9"))
                    .build();

            cognitoClient.forgotPassword(cognitoForgotPasswordRequest);

            return ResponseEntity.ok(new MessageResponse("Forgot password process initiated successfully! Check your email for further instructions."));
        } catch (CognitoIdentityProviderException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error initiating forgot password process: " + e.awsErrorDetails().errorMessage()));
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            // Validate the verification code and reset the password in AWS Cognito
            ConfirmForgotPasswordRequest confirmForgotPasswordRequest = ConfirmForgotPasswordRequest.builder()
                    .clientId("3uq79ioes8do9s35vtjnb4vfgr")
                    .username(resetPasswordRequest.getUsername())
                    .password(resetPasswordRequest.getNewPassword())
                    .secretHash(CognitoUtils.calculateSecretHash(resetPasswordRequest.getUsername(), "3uq79ioes8do9s35vtjnb4vfgr", "rcaib42uqirub26m891234igliv803i324op1ff8mtr8hp4hvq9"))
                    .confirmationCode(resetPasswordRequest.getVerificationCode())
                    .build();

            cognitoClient.confirmForgotPassword(confirmForgotPasswordRequest);

            // Update the password in your local repository
            Optional<User> optionalUser = userRepository.findByUsername(resetPasswordRequest.getUsername());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
            }

            User user = optionalUser.get();
            user.setPassword(encoder.encode(resetPasswordRequest.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
        } catch (CognitoIdentityProviderException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error resetting password in Cognito: " + e.awsErrorDetails().errorMessage()));
        }
    }

}