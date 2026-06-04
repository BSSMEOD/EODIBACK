package com.eod.eod.domain.auth.infrastructure;

import com.eod.eod.domain.auth.model.MobileAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MobileAuthTokenRepository extends JpaRepository<MobileAuthToken, Long> {
    Optional<MobileAuthToken> findByOneTimeToken(String oneTimeToken);
}
