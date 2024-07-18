package com.bezkoder.springjwt.security.jwt;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${bezkoder.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Autowired
  private UserRepository userRepository;

  private PrivateKey loadPrivateKey() throws Exception {
    InputStream inputStream = getClass().getResourceAsStream("/private_key.pem");
    byte[] keyBytes = inputStream.readAllBytes();
    inputStream.close();

    String privateKeyPEM = new String(keyBytes);
    privateKeyPEM = privateKeyPEM
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "");

    byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);

    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }

  public String generateJwtToken(Authentication authentication) throws Exception {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(loadPrivateKey(), SignatureAlgorithm.RS256)
            .compact();
  }

  public String getUserNameFromJwtToken(String token) throws Exception {
    return Jwts.parserBuilder().setSigningKey(loadPrivateKey()).build()
            .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(loadPrivateKey()).build().parse(authToken);
      return true;
    } catch (Exception e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    }

    return false;
  }

  public UserDetailsImpl getUserDetailsFromOAuthToken(String oauthToken) {
    String username = extractUsernameFromToken(oauthToken); // Example method to extract username
    // Fetch user details from UserRepository based on the username
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

    // Create UserDetailsImpl object with user details
    UserDetailsImpl userDetails = new UserDetailsImpl(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            null // Replace null with user roles or authorities if needed
    );
    return userDetails;
  }

  private String extractUsernameFromToken(String oauthToken) {
    //need to write the logic for this
    return oauthToken;
  }

  public static long getExpirationTime(String token) {
    Jws<Claims> claimsJws = Jwts.parserBuilder().build().parseClaimsJws(token);
    Claims claims = claimsJws.getBody();
    return claims.getExpiration().getTime();
  }
}


