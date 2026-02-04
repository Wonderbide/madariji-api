package com.backcover.service;

import com.backcover.model.User;
import com.backcover.model.UserDailyQuota;
import com.backcover.repository.UserDailyQuotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class QuotaService {

    private static final Logger log = LoggerFactory.getLogger(QuotaService.class);

    // Quotas per tier
    private static final int FREE_USER_DAILY_LIMIT = 5;
    private static final int STANDARD_USER_DAILY_LIMIT = 100;
    private static final int PRO_USER_DAILY_LIMIT = 500; // Effectively unlimited for normal use

    private final UserDailyQuotaRepository quotaRepository;

    public QuotaService(UserDailyQuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    /**
     * Check if user can consult a word (has remaining quota)
     */
    public boolean canConsultWord(User user) {
        UserDailyQuota quota = getOrCreateTodayQuota(user);
        int limit = getDailyLimitForUser(user);
        return quota.getWordConsultations() < limit;
    }

    /**
     * Increment word consultation counter for user
     * @return true if successful, false if quota exceeded
     */
    @Transactional
    public boolean incrementWordConsultation(User user) {
        UserDailyQuota quota = getOrCreateTodayQuota(user);
        int limit = getDailyLimitForUser(user);

        if (quota.getWordConsultations() >= limit) {
            log.info("User {} has reached daily quota of {} words", user.getEmail(), limit);
            return false;
        }

        quota.incrementWordConsultations();
        quotaRepository.save(quota);

        log.debug("User {} word consultations: {}/{}",
            user.getEmail(), quota.getWordConsultations(), limit);

        return true;
    }

    /**
     * Get quota info for user
     */
    public QuotaInfo getQuotaInfo(User user) {
        UserDailyQuota quota = getOrCreateTodayQuota(user);
        int limit = getDailyLimitForUser(user);
        int remaining = limit - quota.getWordConsultations();
        String tier = getTierForUser(user);
        return new QuotaInfo(quota.getWordConsultations(), Math.max(0, remaining), limit, tier, isPaidUser(user));
    }

    private UserDailyQuota getOrCreateTodayQuota(User user) {
        LocalDate today = LocalDate.now();

        return quotaRepository.findByUserAndQuotaDate(user, today)
            .orElseGet(() -> {
                UserDailyQuota newQuota = new UserDailyQuota(user, today);
                return quotaRepository.save(newQuota);
            });
    }

    private int getDailyLimitForUser(User user) {
        String role = user.getRole();
        if (role == null) return FREE_USER_DAILY_LIMIT;

        return switch (role) {
            case "ROLE_PRO", "ROLE_ADMIN" -> PRO_USER_DAILY_LIMIT;
            case "ROLE_STANDARD" -> STANDARD_USER_DAILY_LIMIT;
            case "ROLE_PREMIUM" -> STANDARD_USER_DAILY_LIMIT; // Backward compatibility
            default -> FREE_USER_DAILY_LIMIT;
        };
    }

    private String getTierForUser(User user) {
        String role = user.getRole();
        if (role == null) return "free";

        return switch (role) {
            case "ROLE_PRO", "ROLE_ADMIN" -> "pro";
            case "ROLE_STANDARD", "ROLE_PREMIUM" -> "standard";
            default -> "free";
        };
    }

    private boolean isPaidUser(User user) {
        String role = user.getRole();
        return role != null && (
            role.equals("ROLE_PRO") ||
            role.equals("ROLE_STANDARD") ||
            role.equals("ROLE_PREMIUM") ||
            role.equals("ROLE_ADMIN")
        );
    }

    /**
     * DTO for quota information
     */
    public record QuotaInfo(int used, int remaining, int limit, String tier, boolean isPremium) {}
}
