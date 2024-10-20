package rj.com.store.services.servicesimp;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rj.com.store.datatransferobjects.PageableResponse;
import rj.com.store.datatransferobjects.UserDTO;
import rj.com.store.enities.Providers;
import rj.com.store.enities.Role;
import rj.com.store.enities.User;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.helper.AppCon;
import rj.com.store.helper.Helper;
import rj.com.store.repositories.RoleRepository;
import rj.com.store.repositories.UserRepositories;
import rj.com.store.services.ImageServiceInCloud;
import rj.com.store.services.UserService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the UserService interface for managing user-related operations.
 * This class handles creating, updating, deleting, searching, and retrieving users.
 */
@Service
public class UserServiceImp implements UserService {


    @Value("${user.profile.image.path}")
    private String imagePath;
    private final UserRepositories userRepositories;
    private final ImageServiceInCloud imageServiceInCloud;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    /**
     * Constructor for the UserServiceImp class.
     *
     * @param userRepositories       the repository for managing user data
     * @param imageServiceInCloud    the service for handling image uploads to the cloud
     * @param passwordEncoder        the encoder for hashing user passwords
     * @param roleRepository         the repository for managing user roles
     * @param modelMapper            the ModelMapper instance for converting between entity and DTO
     */
    public UserServiceImp(UserRepositories userRepositories,
                          ImageServiceInCloud imageServiceInCloud,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository,
                          ModelMapper modelMapper) {
        this.userRepositories = userRepositories;
        this.imageServiceInCloud = imageServiceInCloud;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new user with a default "NORMAL" role.
     *
     * @param userDTO The data transfer object containing user details.
     * @return The created UserDTO object.
     */
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // Generate unique ID for the user
        String id = UUID.randomUUID().toString();
        userDTO.setUserId(id);
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Convert DTO to entity
        User user = modelMapper.map(userDTO, User.class);

        // Assign "NORMAL" role to the user
        Role role1 = roleRepository.findByRoleName("ROLE_" + AppCon.ROLE_NORMAL).orElse(null);
        if (role1 == null) {
            role1 = new Role();
            role1.setRoleId(UUID.randomUUID().toString());
            role1.setRoleName("ROLE_" + AppCon.ROLE_NORMAL);
            roleRepository.save(role1);
        }
        user.setRoles(List.of(role1));

        // Save user in the repository
        User saveUser = userRepositories.save(user);

        // Convert entity back to DTO
        return modelMapper.map(saveUser, UserDTO.class);
    }

    /**
     * Updates an existing user's details by their ID.
     *
     * @param userDTO The data transfer object containing updated user details.
     * @param userId The ID of the user to update.
     * @return The updated UserDTO object.
     */
    @Override
    public UserDTO UpdateUser(UserDTO userDTO, String userId) {
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not found"));

        // Update user details
        user.setName(userDTO.getName());
        user.setAbout(userDTO.getAbout());

        // Handle image update logic
        if ( userDTO.getImageName() != null) {
            if (user.getImageName()==null ){
                user.setImageName(userDTO.getImageName());
            }
            else if(!user.getImageName().equals(userDTO.getImageName())){
                imageServiceInCloud.deleteImage(user.getImageName());
                user.setImageName(userDTO.getImageName());
            }

        }

        // Handle password update logic

            if(!userDTO.getPassword().equalsIgnoreCase(user.getPassword()) ){
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }


        // Save the updated user in the repository
        User updatedUser = userRepositories.save(user);

        // Convert updated entity to DTO
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     */
    @Override
    public void deleteUser(String userId) {
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found exception"));

        // Delete the user's image from cloud storage if applicable
        if (user.getImageName() != null && !user.getImageName().equalsIgnoreCase("default-image-url")) {
            imageServiceInCloud.deleteImage(user.getImageName());
        }

        // Delete the user from the repository
        userRepositories.delete(user);
    }

    /**
     * Retrieves a pageable list of all users.
     *
     * @param pageNumber The page number to retrieve (0-based).
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort the results by.
     * @param sortDir The direction to sort ("asc" for ascending, "desc" for descending).
     * @return A pageable response containing a list of UserDTO objects.
     */
    @Override
    public PageableResponse<UserDTO> getAllUser(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Page<User> page = userRepositories.findAll(PageRequest.of(
                pageNumber, pageSize, sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()));

        // Convert the page result to a pageable response using a helper method
        return Helper.getPageableResponse(page, UserDTO.class);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserDTO object for the requested user.
     */
    @Override
    public UserDTO getUserById(String userId) {
        User user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found with given User Id"));
        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Retrieves a user by their email.
     *
     * @param userEmail The email of the user to retrieve.
     * @return The UserDTO object for the requested user.
     */
    @Override
    public UserDTO getUserByEmail(String userEmail) {
        User user = userRepositories.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given user email"));
        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Searches for users by a keyword and returns a pageable list of results.
     *
     * @param keyword The keyword to search for in usernames or other details.
     * @param pageNumber The page number to retrieve (0-based).
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort the results by.
     * @param sortDir The direction to sort ("asc" for ascending, "desc" for descending).
     * @return A pageable response containing a list of UserDTO objects matching the search criteria.
     */
    @Override
    public PageableResponse<UserDTO> searchUser(String keyword, int pageNumber, int pageSize, String sortBy, String sortDir) {
        Page<User> page = userRepositories.findByNameContaining(keyword, PageRequest.of(
                        pageNumber, pageSize, sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given search criteria"));

        // Convert the page result to a pageable response
        return Helper.getPageableResponse(page, UserDTO.class);
    }
}
