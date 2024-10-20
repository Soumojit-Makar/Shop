package rj.com.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rj.com.store.datatransferobjects.ImageResponse;
import rj.com.store.datatransferobjects.UserDTO;
import rj.com.store.services.ImageServiceInCloud;
import rj.com.store.services.UserService;

/**
 * Controller for handling image upload-related APIs.
 *
 * This class provides endpoints for uploading images to the cloud.
 * The images are uploaded using cloud services, and metadata is returned upon success.
 */
@RestController
@RequestMapping("/image/v1")
@SecurityRequirement(name = "scheme")
@Tag(name = "Image Controller", description = "This API handles image operations such as uploading images to the cloud.")
public class ImageController {

    private final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final ImageServiceInCloud imageServiceInCloud;
    private final UserService userService;

    /**
     * Constructor to initialize ImageServiceInCloud and UserService.
     *
     * @param imageServiceInCloud the service used for cloud-based image upload operations
     * @param userService the service used to manage user details
     */
    public ImageController(ImageServiceInCloud imageServiceInCloud, UserService userService) {
        this.imageServiceInCloud = imageServiceInCloud;
        this.userService = userService;
    }

    /**
     * Uploads an image to the cloud and updates the user profile with the uploaded image URL.
     *
     * @param userId the ID of the user whose profile image is being updated
     * @param imageRequest the image file to be uploaded
     * @return a ResponseEntity containing the image name and success message
     */
    @PostMapping(value = "/upload/user-image/{userId}")
    @Operation(summary = "Upload a user profile image")
    public ResponseEntity<ImageResponse> uploadImage(
            @PathVariable("userId") String userId,
            @RequestParam("ImageRequest") MultipartFile imageRequest) {

        logger.info("Uploading image for user ID: {}, Image: {}", userId, imageRequest.getOriginalFilename());

        // Upload the image to cloud storage
        String imageUrl = imageServiceInCloud.uploadImage(imageRequest);

        // Retrieve the user by userId and update the image URL
        UserDTO userDTO = userService.getUserById(userId);
        userDTO.setImageName(imageUrl);
        userService.UpdateUser(userDTO, userId);

        // Build and return the response
        return new ResponseEntity<>(ImageResponse.builder()
                .imageName(imageUrl)
                .massage("Image successfully uploaded")
                .success(true)
                .httpStatus(HttpStatus.CREATED)
                .build(),
                HttpStatus.CREATED);
    }
}
