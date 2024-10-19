package rj.com.store.enities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String refreshTokenHold;
    private Instant expiresDate;
    @OneToOne
    private User user;
}
