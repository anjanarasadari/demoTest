package com.payable.demotest.service;

import org.springframework.security.core.Authentication;

/**
 * Service interface for JWT token operations.
 * Provides abstraction for token generation, validation, and claim extraction.
 */
public interface JwtService {
    /**
     * Generate a JWT token from Authentication object.
     *
     * @param authentication the authentication object containing user details
     * @return the generated JWT token
     */
    String generateToken(Authentication authentication);

    /**
     * Generate a JWT token for a specific username.
     *
     * @param username the username for which to generate the token
     * @return the generated JWT token
     */
    String generateToken(String username);

    /**
     * Extract username from a JWT token.
     *
     * @param token the JWT token
     * @return the username from the token
     */
    String getUsernameFromToken(String token);

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);
}

