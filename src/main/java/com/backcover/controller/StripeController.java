package com.backcover.controller;

import com.backcover.model.User;
import com.backcover.service.StripeService;
import com.backcover.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private static final Logger log = LoggerFactory.getLogger(StripeController.class);

    private final StripeService stripeService;
    private final UserService userService;

    public StripeController(StripeService stripeService, UserService userService) {
        this.stripeService = stripeService;
        this.userService = userService;
    }

    /**
     * Create a Stripe Checkout Session and return the URL to redirect to
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestBody(required = false) Map<String, String> body) {

        if (jwtPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = jwtPrincipal.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token");
        }

        User user = userService.findOrCreateUserByEmail(email);

        // Check if user already has a paid subscription
        if ("ROLE_STANDARD".equals(user.getRole()) || "ROLE_PRO".equals(user.getRole()) || "ROLE_ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has an active subscription");
        }

        // Get tier from request body, default to "standard"
        String tier = (body != null && body.containsKey("tier")) ? body.get("tier") : "standard";
        if (!tier.equals("standard") && !tier.equals("pro")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tier. Must be 'standard' or 'pro'");
        }

        try {
            String checkoutUrl = stripeService.createCheckoutSession(user, tier);
            return ResponseEntity.ok(Map.of("url", checkoutUrl));
        } catch (IllegalStateException e) {
            log.error("Stripe configuration error: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Stripe is not properly configured");
        } catch (StripeException e) {
            log.error("Stripe error creating checkout session: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create checkout session");
        }
    }

    /**
     * Stripe Webhook endpoint - receives events from Stripe
     * This endpoint must be publicly accessible (no auth)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            stripeService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Received");
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
        }
    }

    /**
     * Check subscription status for current user
     */
    @GetMapping("/subscription-status")
    public ResponseEntity<Map<String, Object>> getSubscriptionStatus(
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        if (jwtPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = jwtPrincipal.getClaimAsString("email");
        User user = userService.findOrCreateUserByEmail(email);

        String role = user.getRole();
        boolean isPaid = "ROLE_STANDARD".equals(role) || "ROLE_PRO".equals(role) || "ROLE_ADMIN".equals(role);
        String tier = switch (role) {
            case "ROLE_PRO", "ROLE_ADMIN" -> "pro";
            case "ROLE_STANDARD" -> "standard";
            default -> "free";
        };

        return ResponseEntity.ok(Map.of(
            "isPremium", isPaid, // kept for backward compatibility
            "isPaid", isPaid,
            "tier", tier,
            "role", role,
            "subscriptionStatus", user.getSubscriptionStatus() != null ? user.getSubscriptionStatus() : "none",
            "hasStripeCustomer", user.getStripeCustomerId() != null
        ));
    }

    /**
     * Cancel the user's subscription (at period end)
     */
    @PostMapping("/cancel-subscription")
    public ResponseEntity<Map<String, String>> cancelSubscription(
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        if (jwtPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = jwtPrincipal.getClaimAsString("email");
        User user = userService.findOrCreateUserByEmail(email);

        if (user.getStripeSubscriptionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active subscription found");
        }

        try {
            stripeService.cancelSubscription(user);
            return ResponseEntity.ok(Map.of("message", "Subscription will be canceled at end of billing period"));
        } catch (StripeException e) {
            log.error("Error canceling subscription: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel subscription");
        }
    }

    /**
     * Get URL for Stripe Customer Portal (for subscription management)
     */
    @GetMapping("/customer-portal")
    public ResponseEntity<Map<String, String>> getCustomerPortal(
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        if (jwtPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String email = jwtPrincipal.getClaimAsString("email");
        User user = userService.findOrCreateUserByEmail(email);

        if (user.getStripeCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Stripe customer found");
        }

        try {
            String portalUrl = stripeService.createCustomerPortalSession(user);
            return ResponseEntity.ok(Map.of("url", portalUrl));
        } catch (StripeException e) {
            log.error("Error creating customer portal session: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create portal session");
        }
    }
}
