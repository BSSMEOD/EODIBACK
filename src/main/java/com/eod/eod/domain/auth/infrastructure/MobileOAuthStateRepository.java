package com.eod.eod.domain.auth.infrastructure;

import com.eod.eod.domain.auth.model.MobileOAuthState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MobileOAuthStateRepository extends JpaRepository<MobileOAuthState, String> {
}
