package rj.com.store.datatransferobjects;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JWTResponse
{
    private String jwtToken;
    private UserDTO user;
    private RefreshTokenDTO refreshToken;
}
