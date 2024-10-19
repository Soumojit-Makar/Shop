package rj.com.store.datatransferobjects;

import lombok.*;

import java.time.Instant;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenDTO {
        private int id;
        private String refreshTokenHold;
        private Instant expiresDate;
}
