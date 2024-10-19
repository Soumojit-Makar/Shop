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
import rj.com.store.datatransferobjects.ImageRequest;
import rj.com.store.datatransferobjects.ImageResponse;
import rj.com.store.services.ImageServiceInCloud;
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
    private final Logger logger= LoggerFactory.getLogger(ImageController.class);

    private final ImageServiceInCloud imageServiceInCloud;

    /**
     * Constructor to initialize ImageServiceInCloud.
     *
     * @param imageServiceInCloud the service used for cloud-based image upload operations
     */
    public ImageController(ImageServiceInCloud imageServiceInCloud){
        this.imageServiceInCloud=imageServiceInCloud;

    }

    /**
     * Uploads an image to the cloud.
     *
     * This endpoint accepts an image in the request body and uploads it to the cloud.
     * Upon successful upload, it returns the image name and status information.
     *
     * @param imageRequest the image request containing the image file to be uploaded
     * @return a ResponseEntity containing the image name and a success message
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload user")
    public ResponseEntity<ImageResponse> uploadImage(@Valid @RequestBody ImageRequest imageRequest) {

        return new ResponseEntity<>(ImageResponse.builder()
                .imageName(imageServiceInCloud.uploadImage(imageRequest.image))
                .massage("Image successfully uploaded")
                .success(true)
                .httpStatus(HttpStatus.CREATED)
                .build(),
                HttpStatus.CREATED);
    }

    public Logger getLogger() {
        return logger;
    }
}
