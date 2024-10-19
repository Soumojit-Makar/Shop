package rj.com.store.services.servicesimp;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.repositories.UserRepositories;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepositories userRepositories;
    public CustomUserDetailService(UserRepositories userRepositories) {
        this.userRepositories = userRepositories;
    }
    /**
     * Loads a user by their username (email).
     *
     * @param username the email of the user to be loaded
     * @return the UserDetails object containing user information
     * @throws UsernameNotFoundException if the user with the specified email is not found
     * @throws ResourceNotFoundException if the user cannot be retrieved from the repository
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepositories.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User not found "+username));
    }
}
