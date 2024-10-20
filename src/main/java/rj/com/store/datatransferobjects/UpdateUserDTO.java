package rj.com.store.datatransferobjects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateUserDTO {
    private String userId;
    @NotBlank(message = "Name is required")
    @Size(min = 3, message = "Minimum 3 character required")
    @Schema(name = "username",accessMode = Schema.AccessMode.READ_ONLY,description = "user name for Database")
    private String name;
    @Email(message = "Invalid Email")
    @NotBlank(message = "Email is required")
    private String email;
    private String password;
    @Size(min = 4, max = 6, message = "Inavaid")
    private String gender;
    @NotBlank(message = "Write someting  about yourself")
    private String about;
    private String imageName;
    private List<RoleDTO> roles=new ArrayList<>();
}
