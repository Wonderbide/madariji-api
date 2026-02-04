package com.backcover.repository;

import com.backcover.model.User;
import com.backcover.model.UserDailyQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDailyQuotaRepository extends JpaRepository<UserDailyQuota, UUID> {

    Optional<UserDailyQuota> findByUserAndQuotaDate(User user, LocalDate quotaDate);

    Optional<UserDailyQuota> findByUserIdAndQuotaDate(UUID userId, LocalDate quotaDate);
}
