package rj.com.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rj.com.store.datatransferobjects.ApiResponseMessage;
import rj.com.store.datatransferobjects.ImageResponse;
import rj.com.store.datatransferobjects.PageableResponse;
import rj.com.store.datatransferobjects.UserDTO;
import rj.com.store.enities.Providers;
import rj.com.store.helper.AppCon;
import rj.com.store.services.FileService;
import rj.com.store.services.UserService;

import java.io.IOException;

/**
 * Controller for managing user-related operations.
 */
@RestController
@RequestMapping("/users/v1")
@SecurityRequirement(name = "scheme")
@Tag(name = "Users Controller", description = "This is user API for user operations")
public class UserController {
    private final Logger logger= LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final FileService fileService;
    @Value("${user.profile.image.path}")
    private String imageUploadPath;
    @Enumerated(EnumType.STRING)
    Providers provider= Providers.SELF;
    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService=fileService;
    }
    /**
     * Create a new user.
     * @param userDTO the user details
     * @return the created user details
     */
    @PostMapping
    @Operation(summary = "create new user")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        userDTO.setProvider(provider);
        UserDTO user=userService.createUser(userDTO);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
    /**
     * Update user details.
     * @param userDTO the updated user details
     * @param userId the ID of the user to update
     * @return the updated user details
     */
    @PutMapping("/{userId}")
    @Operation(summary = "update user")
    public ResponseEntity<UserDTO> updateUser(
            @Valid @RequestBody UserDTO userDTO,
            @PathVariable("userId") String userId) {
        return new ResponseEntity<>(userService.UpdateUser(userDTO, userId), HttpStatus.OK);
    }
    /**
     * Delete a user.
     * @param userId the ID of the user to delete
     * @return a response message indicating success
     * @throws IOException if an I/O error occurs
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "delete user")
    public ResponseEntity<ApiResponseMessage> deleteUser(
            @PathVariable("userId") String userId) throws IOException {

        userService.deleteUser(userId);

        return new ResponseEntity<>(ApiResponseMessage.builder()
                .success(true)
                .massage("User is deleted successfully")
                .httpStatus(HttpStatus.OK)
                .build(), HttpStatus.OK);

    }
    /**
     * Get all users.
     * @param pageNumber the page number to retrieve
     * @param pageSize the number of users per page
     * @param sortBy the field to sort by
     * @param sortDir the direction to sort (asc/desc)
     * @return a paginated list of users
     */
    @GetMapping
    @Operation(summary = "get all users")
    public ResponseEntity<PageableResponse<UserDTO>> getAllUsers(
            @RequestParam(value = "pageNumber",defaultValue = AppCon.Page_Number,required = false) int pageNumber,
            @RequestParam(value = "pageSize",defaultValue = AppCon.Page_Size,required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue=AppCon.Sort_By,required = false) String sortBy ,
            @RequestParam(value = "sortDir", defaultValue=AppCon.Sort_Dir,required = false) String sortDir)
    {
        return new ResponseEntity<>(userService
                .getAllUser(pageNumber,pageSize,sortBy,sortDir),
                HttpStatus.OK);
    }
    /**
     * Get a single user by ID.
     * @param userId the ID of the user to retrieve
     * @return the user details
     */
    @GetMapping("/{userId}")
    @Operation(summary = "get single user")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable("userId") String userId) {
        return new ResponseEntity<>(userService
                .getUserById(userId),
                HttpStatus.OK);
    }
    /**
     * Get a user by email.
     * @param email the email of the user to retrieve
     * @return the user details
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "get user by emailId")
    public ResponseEntity<UserDTO> getUserByEmail(
            @PathVariable("email") String email) {
        return new ResponseEntity<>(userService
                .getUserByEmail(email),
                HttpStatus.OK);
    }
    /**
     * Search for users by keyword.
     * @param keyword the keyword to search for
     * @param pageNumber the page number to retrieve
     * @param pageSize the number of users per page
     * @param sortBy the field to sort by
     * @param sortDir the direction to sort (asc/desc)
     * @return a paginated list of users matching the search criteria
     */
    @GetMapping("/search/{keyword}")
    @Operation(summary = "search user by key word")
    public ResponseEntity<PageableResponse<UserDTO>> searchUser(
            @PathVariable("keyword") String keyword,
            @RequestParam(value = "pageNumber",defaultValue = AppCon.Page_Number,required = false) int pageNumber,
            @RequestParam(value = "pageSize",defaultValue = AppCon.Page_Size,required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue=AppCon.Sort_By,required = false) String sortBy ,
            @RequestParam(value = "sortDir", defaultValue=AppCon.Sort_Dir,required = false) String sortDir) {
        return new ResponseEntity<>(
                userService
                        .searchUser(keyword, pageNumber, pageSize, sortBy, sortDir),
                HttpStatus.OK);
    }
    /**
     * Upload a user's profile image.
     * @param userId the ID of the user
     * @param image the image file to upload
     * @return a response containing the uploaded image information
     * @throws IOException if an I/O error occurs during image upload
     */
    @PostMapping("/image/{userId}")
    @Operation(summary = "Upload user image")
    public ResponseEntity<ImageResponse> uploadUserImage(@PathVariable("userId") String userId,
            @RequestParam("userImage")MultipartFile image) throws IOException{
           String imageName=fileService.uploadImage(image, imageUploadPath);
           logger.info("Image path name {}",imageName);
           UserDTO userDTO=userService.getUserById(userId);
           userDTO.setImageName(imageName);
           userService.UpdateUser(userDTO,userId);
            return  new ResponseEntity<>(
                    ImageResponse
                            .builder()
                            .imageName(imageName)
                            .massage("Image uploaded successfully")
                            .success(true)
                            .httpStatus(HttpStatus.CREATED)
                            .build(),
                    HttpStatus.CREATED
            );
    }

}
