package rj.com.store.datatransferobjects;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JWTRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
