package rj.com.store.services.servicesimp;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import rj.com.store.datatransferobjects.RefreshTokenDTO;
import rj.com.store.datatransferobjects.UserDTO;
import rj.com.store.enities.RefreshToken;
import rj.com.store.enities.User;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.repositories.RefreshTokenRepository;
import rj.com.store.repositories.UserRepositories;
import rj.com.store.services.RefreshTokenService;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Implementation of the RefreshTokenService interface.
 * Provides methods for creating, verifying, and managing refresh tokens for user authentication.
 */
@Service
public class RefreshTokenServiceImp implements RefreshTokenService {

    RefreshTokenRepository refreshTokenRepository;
    UserRepositories userRepositories;
    ModelMapper modelMapper;
    /**
     * Constructor for the RefreshTokenServiceImp class.
     *
     * @param refreshTokenRepository the repository for managing refresh tokens
     * @param userRepositories the repository for accessing user data
     * @param modelMapper the ModelMapper instance for converting between entity and DTO
     */
    public RefreshTokenServiceImp(RefreshTokenRepository refreshTokenRepository,
                                  UserRepositories userRepositories,
                                  ModelMapper modelMapper) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepositories = userRepositories;
        this.modelMapper=modelMapper;

    }

    /**
     * Creates a new refresh token for the given username.
     * If the user already has a refresh token, update the existing token with a new one.
     *
     * @param userName The username for which to create a refresh token.
     * @return A DTO object containing the newly created or updated refresh token.
     */
    @Override
    public RefreshTokenDTO createRefreshToken(String userName) {
        User user = userRepositories.findByEmail(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElse(null);
        Instant now = Instant.now().plusSeconds(30 * 24 * 3600);
        if (refreshToken == null) {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .refreshTokenHold(UUID.randomUUID().toString())
                    .expiresDate(now) // Token valid for 30 days
                    .build();
        } else {
            refreshToken.setRefreshTokenHold(UUID.randomUUID().toString());
            refreshToken.setExpiresDate(now); // Reset expiration
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        return modelMapper.map(savedRefreshToken, RefreshTokenDTO.class);
    }

    /**
     * Retrieves a refresh token by its token string.
     *
     * @param token The token string to search for.
     * @return A DTO object representing the refresh token.
     */
    @Override
    public RefreshTokenDTO findByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshTokenHold(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token not found"));
        return modelMapper.map(refreshToken, RefreshTokenDTO.class);
    }

    /**
     * Verifies if a refresh token is valid and not expired.
     * Deletes the token if it has expired.
     *
     * @param token The refresh token to verify.
     * @return A DTO object representing the verified refresh token.
     */
    @Override
    public RefreshTokenDTO verifyRefreshToken(RefreshTokenDTO token) {
        var refreshToken = modelMapper.map(token, RefreshToken.class);

        // Check if the token has expired
        if (token.getExpiresDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new ResourceNotFoundException("Refresh Token Expired");
        }

        return token;
    }

    /**
     * Retrieves the user associated with a given refresh token.
     *
     * @param token The refresh token to use for retrieving the user.
     * @return A DTO object representing the user.
     */
    @Override
    public UserDTO getUserByToken(RefreshTokenDTO token) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshTokenHold(token.getRefreshTokenHold())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token not found"));

        User user = refreshToken.getUser();
        return modelMapper.map(user, UserDTO.class);
    }
}
