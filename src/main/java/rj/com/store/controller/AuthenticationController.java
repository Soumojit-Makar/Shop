package rj.com.store.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import rj.com.store.datatransferobjects.*;
import rj.com.store.enities.Providers;
import rj.com.store.enities.User;
import rj.com.store.exceptions.BadApiRequest;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.security.JwtHelper;
import rj.com.store.services.RefreshTokenService;
import rj.com.store.services.UserService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
/**
 * Controller for handling authentication-related APIs.
 *
 * This class provides endpoints for user authentication, including login,
 * registration, and token management.
 * It uses security requirements to protect sensitive operations and uses JWT for authentication.
 *
 * @see JwtHelper
 */
@RestController
@RequestMapping("/auth/v1")
@SecurityRequirement(name = "scheme")
@Tag(name = "Authentication", description = "APIs for authentication")
public class AuthenticationController {
    @Value("${app.google.client_id}")
    private String googleClientId;
    @Value("${app.google.password}")
    private String googlePassword;
    private final Logger logger= LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    @Enumerated(EnumType.STRING)
    private static final Providers provider=Providers.GOOGLE;
    /**
     * Constructs an instance of {@link AuthenticationController} with the specified dependencies.
     *
     * @param authenticationManager     the manager for handling authentication requests
     * @param jwtHelper                 the utility for JWT operations, including token generation and validation
     * @param userDetailsService        the service for loading user-specific data
     * @param modelMapper               the model mapper for converting between entities and DTOs
     * @param userService               the service for managing user-related operations
     * @param refreshTokenService       the service for handling refresh token operations
     */
    public AuthenticationController(
            AuthenticationManager authenticationManager,JwtHelper jwtHelper,
            UserDetailsService userDetailsService,ModelMapper modelMapper,
            UserService userService, RefreshTokenService refreshTokenService){
        this.authenticationManager=authenticationManager;
        this.jwtHelper=jwtHelper;
        this.userDetailsService=userDetailsService;
        this.modelMapper=modelMapper;
        this.userService=userService;
        this.refreshTokenService=refreshTokenService;
    }
    /**
     * Authenticates a user and generates a JWT token.
     *
     * This endpoint takes a JWTRequest object containing the user's email and password,
     * authenticates the user, and generates a JWT token along with a refresh token.
     *
     * @param jwtRequest the request object containing the user's email and password
     * @return a ResponseEntity containing the JWTResponse with the generated token,
     *         refresh token, and user details
     * @throws BadApiRequest if authentication fails or if the provided credentials are invalid
     */
    @PostMapping("/generate-token")
    @Operation(summary = "Generate Token by JWTRequest")
    public ResponseEntity<JWTResponse> login(@RequestBody JWTRequest jwtRequest) {
        this.doAuthenticate(jwtRequest.getEmail(),jwtRequest.getPassword());
        User user = (User) this.userDetailsService.loadUserByUsername(jwtRequest.getEmail());
        String token = jwtHelper.generateToken(user);
        RefreshTokenDTO refreshTokenDTO= refreshTokenService.createRefreshToken(user.getEmail());
        return ResponseEntity.ok(
                JWTResponse
                        .builder()
                        .refreshToken(refreshTokenDTO)
                        .jwtToken(token)
                        .user(modelMapper.map(user, UserDTO.class))
                        .build());
    }
    /**
     * Regenerates a JWT token using a valid refresh token.
     *
     * This endpoint accepts a RefreshTokenRequest containing a refresh token,
     * verifies the token, and generates a new JWT token for the user.
     *
     * @param refreshTokenRequest the request object containing the refresh token
     * @return a ResponseEntity containing the JWTResponse with the new JWT token,
     *         user details, and the original refresh token
     * @throws BadApiRequest if the refresh token is invalid or expired
     * @throws ResourceNotFoundException if the user associated with the refresh token cannot be found
     */
    @PostMapping("/regenerate-token")
    @Operation(summary = "Generate Token by Refresh Token Request")
    public ResponseEntity<JWTResponse>regenerateToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshTokenDTO refreshTokenDTO=refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken());
        RefreshTokenDTO refreshTokenDTO1=refreshTokenService.verifyRefreshToken(refreshTokenDTO);
        UserDTO userDTO= refreshTokenService.getUserByToken(refreshTokenDTO1);
        String jwtToken= jwtHelper.generateToken(modelMapper.map(userDTO,User.class));
        return ResponseEntity.ok(JWTResponse.builder().jwtToken(jwtToken)
                .user(userDTO)
                .refreshToken(refreshTokenDTO)
                .build());
    }
    /**
     * Handles login requests via Google authentication.
     *
     * This method verifies the provided Google ID token, extracts user information,
     * and either retrieves an existing user or creates a new user in the system.
     * Upon successful verification, a JWT token and refresh token are generated
     * for the authenticated user.
     *
     * @param googleLoginRequest the request containing the Google ID token
     * @return a ResponseEntity containing the JWT response, including the JWT token,
     *         refresh token, and user information
     * @throws GeneralSecurityException if a security-related error occurs
     * @throws IOException if an I/O error occurs during token verification
     * @throws BadApiRequest if the Google login verification fails
     */
    @PostMapping("/login-with-google")
    @Operation(summary = "Generate Token by Google Login Request")
    public ResponseEntity<JWTResponse> handleGoogleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) throws GeneralSecurityException, IOException {
        logger.info("Google login request {}", googleLoginRequest.getIdToken());
        GoogleIdTokenVerifier token= new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(),new GsonFactory()).setAudience(List.of(googleClientId)).build();
        GoogleIdToken idToken=token.verify(googleLoginRequest.getIdToken());

        if(idToken!=null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            UserDTO userDTO=UserDTO.builder()
                    .name(name)
                    .email(email)
                    .imageName(pictureUrl)
                    .password(googlePassword)
                    .about("User created by google")
                    .provider(provider)
                    .build();
            UserDTO user=null;
            try {
                 user= userService.getUserByEmail(email);
            }catch (ResourceNotFoundException ex){
                user= userService.createUser(userDTO);
            }
            logger.info("Email {}, Password {}",user.getEmail(),user.getPassword());

            User userAuth=(User) this.userDetailsService.loadUserByUsername(user.getEmail());
            String authToken = jwtHelper.generateToken(userAuth);
            RefreshTokenDTO refreshTokenDTO= refreshTokenService.createRefreshToken(userAuth.getEmail());
            return ResponseEntity.ok(
                    JWTResponse
                            .builder()
                            .refreshToken(refreshTokenDTO)
                            .jwtToken(authToken)
                            .user(user)
                            .build());
        }else {
            logger.error("Google login failed");
            throw new BadApiRequest("Google login failed");
        }
    }
    /**
     * Authenticates a user based on their email and password.
     *
     * This method attempts to authenticate the user using the provided email and password.
     * If authentication fails due to incorrect credentials,
     * a BadCredentialsException is thrown.
     *
     * @param email    the email of the user trying to authenticate
     * @param password the password of the user trying to authenticate
     * @throws BadCredentialsException if the authentication fails due to
     *                                  incorrect email or password
     */
    private void doAuthenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        }catch (BadCredentialsException e) {
            throw new BadCredentialsException("User not found of given username or password");
        }
    }
}