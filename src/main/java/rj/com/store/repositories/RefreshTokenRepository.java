package rj.com.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rj.com.store.enities.RefreshToken;
import rj.com.store.enities.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken,Integer> {
    Optional<RefreshToken> findByRefreshTokenHold(String refreshTokenHold);
    Optional<RefreshToken> findByUser(User user);
}
