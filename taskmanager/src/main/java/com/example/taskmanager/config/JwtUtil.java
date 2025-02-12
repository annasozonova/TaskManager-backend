package com.example.taskmanager.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for handling JWT (JSON Web Tokens) operations.
 * Provides methods for generating, validating, and extracting claims from JWTs.
 */
@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generates a secret key for signing JWTs

    /**
     * Generates a JWT token for a given username and role.
     * The token includes the username, role, issue date, and expiration time (10 hours).
     *
     * @param username the username for which the token is generated
     * @param role     the role associated with the user
     * @return the generated JWT token
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token the JWT token from which the username is extracted
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the role from the JWT token.
     *
     * @param token the JWT token from which the role is extracted
     * @return the role extracted from the token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class)); // Extracts the role
    }

    /**
     * Validates the JWT token by checking if the username, role, and expiration are correct.
     *
     * @param token    the JWT token to be validated
     * @param username the username to be validated
     * @param role     the role to be validated
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, String username, String role) {
        return extractRole(token).equals(role) && extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token the JWT token to be checked
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token from which the expiration date is extracted
     * @return the expiration date of the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token the JWT token from which the claim is extracted
     * @param claimsResolver the function to extract the claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token from which all claims are extracted
     * @return all claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}