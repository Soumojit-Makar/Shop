package rj.com.store.services.servicesimp;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rj.com.store.exceptions.BadApiRequest;
import rj.com.store.services.ImageServiceInCloud;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;

@Service
public class ImageServiceInCloudImp implements ImageServiceInCloud {
    Logger logger= LoggerFactory.getLogger(ImageServiceInCloudImp.class);
    private final Cloudinary cloudinary;
    public ImageServiceInCloudImp( Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    /**
     * Uploads an image to the cloud storage.
     *
     * This method checks if the provided image file is empty. If not, it reads the image data,
     * generates a unique public ID for the image, and uploads it to Cloudinary.
     * The method returns the publicly accessible URL of the uploaded image.
     *
     * @param image the image file to be uploaded
     * @return the public URL of the uploaded image
     * @throws IllegalArgumentException if the image is empty or null
     */
    @Override
    public String uploadImage(MultipartFile image) {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            byte[] data = new byte[image.getInputStream().available()];

            String fileName = UUID.randomUUID().toString();
            logger.info("File name {}", fileName);

            image.getInputStream().read(data);

            cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "public_id", fileName
            ));

            return this.getURLFromPublicId(fileName);
        } catch (IOException e) {
            logger.error("Error uploading image: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the public URL for an image stored in cloud storage using its public ID.
     *
     * This method generates a publicly accessible URL for the specified image by applying any necessary transformations.
     *
     * @param publicID the public ID of the image stored in the cloud
     * @return the publicly accessible URL of the image
     */
    @Override
    public String getURLFromPublicId(String publicID) {
        logger.info("File name {}", publicID);
        return cloudinary.url().transformation(
                new Transformation<>()
        ).generate(publicID);
    }
    /**
     * Deletes an image from cloud storage based on its public ID.
     *
     * This method checks if the provided public ID is valid, then attempts to delete the image
     * associated with that public ID from cloud storage. If the public ID is empty or only contains whitespace,
     * it returns "Ok" without attempting to delete anything.
     *
     * @param publicId the public ID of the image to be deleted
     * @return a confirmation message indicating the status of the deletion
     * @throws RuntimeException if the image deletion fails
     */
    @Override
    public String deleteImage(String publicId) {
        logger.info("File name {}", publicId);
        if (publicId.isEmpty() || publicId.equalsIgnoreCase(" ")) {
            return "Ok";
        }

            String im = publicId.substring(publicId.lastIndexOf("/") + 1);
            logger.info("Delete file public id {}", im);
        try {
            cloudinary.uploader().destroy(im, ObjectUtils.asMap(
                    "public_id", im
            ));
            return "Image Successfully deleted";
        } catch (Exception e) {
            throw new BadApiRequest("Image Not Deleted");
        }
    }

}
