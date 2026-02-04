package com.backcover.service;

import com.backcover.model.User;
import com.backcover.repository.UserRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.price.standard.monthly:}")
    private String stripePriceStandardMonthly;

    @Value("${stripe.price.pro.monthly:}")
    private String stripePriceProMonthly;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final UserRepository userRepository;

    public StripeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe SDK initialized");
    }

    /**
     * Create a Stripe Checkout Session for subscription
     * @param user The user subscribing
     * @param tier The subscription tier: "standard" or "pro"
     */
    public String createCheckoutSession(User user, String tier) throws StripeException {
        String priceId = getPriceIdForTier(tier);

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomerEmail(user.getEmail())
            .setSuccessUrl(frontendUrl + "/subscription/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(frontendUrl + "/subscription/cancel")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(priceId)
                    .setQuantity(1L)
                    .build()
            )
            .putMetadata("user_id", user.getId().toString())
            .putMetadata("user_email", user.getEmail())
            .putMetadata("tier", tier)
            .build();

        Session session = Session.create(params);
        log.info("Checkout session created for user {} with tier {}: {}", user.getEmail(), tier, session.getId());

        return session.getUrl();
    }

    /**
     * Get the Stripe price ID for a given tier
     */
    private String getPriceIdForTier(String tier) {
        return switch (tier.toLowerCase()) {
            case "standard" -> {
                if (stripePriceStandardMonthly == null || stripePriceStandardMonthly.isBlank()) {
                    throw new IllegalStateException("STRIPE_PRICE_STANDARD_MONTHLY is not configured");
                }
                yield stripePriceStandardMonthly;
            }
            case "pro" -> {
                if (stripePriceProMonthly == null || stripePriceProMonthly.isBlank()) {
                    throw new IllegalStateException("STRIPE_PRICE_PRO_MONTHLY is not configured");
                }
                yield stripePriceProMonthly;
            }
            default -> throw new IllegalArgumentException("Invalid tier: " + tier + ". Must be 'standard' or 'pro'");
        };
    }

    /**
     * Get the role for a given tier
     */
    public static String getRoleForTier(String tier) {
        return switch (tier.toLowerCase()) {
            case "standard" -> "ROLE_STANDARD";
            case "pro" -> "ROLE_PRO";
            default -> "ROLE_FREE";
        };
    }

    /**
     * Create a Stripe Customer Portal session for subscription management
     */
    public String createCustomerPortalSession(User user) throws StripeException {
        if (user.getStripeCustomerId() == null) {
            throw new IllegalStateException("User has no Stripe customer ID");
        }

        com.stripe.param.billingportal.SessionCreateParams params =
            com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(user.getStripeCustomerId())
                .setReturnUrl(frontendUrl + "/my-library")
                .build();

        com.stripe.model.billingportal.Session portalSession =
            com.stripe.model.billingportal.Session.create(params);
        log.info("Customer portal session created for user {}", user.getEmail());

        return portalSession.getUrl();
    }

    /**
     * Cancel a user's subscription (at period end)
     */
    @Transactional
    public void cancelSubscription(User user) throws StripeException {
        if (user.getStripeSubscriptionId() == null) {
            throw new IllegalStateException("User has no active subscription");
        }

        Subscription subscription = Subscription.retrieve(user.getStripeSubscriptionId());

        // Cancel at period end (user keeps access until end of billing period)
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
            .setCancelAtPeriodEnd(true)
            .build();

        subscription.update(params);
        log.info("Subscription {} set to cancel at period end for user {}",
                 user.getStripeSubscriptionId(), user.getEmail());
    }

    /**
     * Handle Stripe webhook event
     */
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        Event event;

        if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
            log.error("STRIPE_WEBHOOK_SECRET is not configured - rejecting webhook");
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
        event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

        log.info("Received Stripe event: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> log.debug("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutCompleted(Event event) {
        log.info("Processing checkout.session.completed event: {}", event.getId());

        var deserializer = event.getDataObjectDeserializer();
        String rawJson = deserializer.getRawJson();

        if (rawJson == null) {
            log.error("No raw JSON in event");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();

            // Extract email
            String email = extractEmail(jsonObject);
            if (email == null) {
                log.error("No email found in checkout session");
                return;
            }

            // Extract Stripe IDs
            String customerId = getJsonString(jsonObject, "customer");
            String subscriptionId = getJsonString(jsonObject, "subscription");

            // Extract tier from metadata
            String tier = "pro"; // default fallback
            if (jsonObject.has("metadata") && !jsonObject.get("metadata").isJsonNull()) {
                JsonObject metadata = jsonObject.getAsJsonObject("metadata");
                String metaTier = getJsonString(metadata, "tier");
                if (metaTier != null) {
                    tier = metaTier;
                }
            }

            String role = getRoleForTier(tier);

            log.info("Checkout completed - email: {}, customer: {}, subscription: {}, tier: {}",
                     email, customerId, subscriptionId, tier);

            userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    user.setRole(role);
                    user.setStripeCustomerId(customerId);
                    user.setStripeSubscriptionId(subscriptionId);
                    user.setSubscriptionStatus("active");
                    userRepository.save(user);
                    log.info("User {} upgraded to {} with subscription {}", email, role, subscriptionId);
                },
                () -> log.error("User not found for email: {}", email)
            );
        } catch (Exception e) {
            log.error("Failed to process checkout.session.completed: {}", e.getMessage(), e);
        }
    }

    private void handleInvoicePaid(Event event) {
        log.info("Processing invoice.paid event: {}", event.getId());

        var deserializer = event.getDataObjectDeserializer();
        String rawJson = deserializer.getRawJson();

        if (rawJson == null) {
            log.error("No raw JSON in event");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();
            String customerId = getJsonString(jsonObject, "customer");

            if (customerId == null) {
                log.error("No customer ID in invoice.paid event");
                return;
            }

            userRepository.findByStripeCustomerId(customerId).ifPresentOrElse(
                user -> {
                    // Keep current role if already STANDARD or PRO (handles renewals)
                    // Only set role if user was FREE (shouldn't happen normally)
                    if ("ROLE_FREE".equals(user.getRole())) {
                        user.setRole("ROLE_STANDARD"); // Default to standard if unknown
                    }
                    user.setSubscriptionStatus("active");
                    userRepository.save(user);
                    log.info("Invoice paid - user {} subscription renewed (role: {})", user.getEmail(), user.getRole());
                },
                () -> log.warn("No user found for customer: {}", customerId)
            );
        } catch (Exception e) {
            log.error("Failed to process invoice.paid: {}", e.getMessage(), e);
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        log.info("Processing invoice.payment_failed event: {}", event.getId());

        var deserializer = event.getDataObjectDeserializer();
        String rawJson = deserializer.getRawJson();

        if (rawJson == null) {
            log.error("No raw JSON in event");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();
            String customerId = getJsonString(jsonObject, "customer");

            if (customerId == null) {
                log.error("No customer ID in invoice.payment_failed event");
                return;
            }

            userRepository.findByStripeCustomerId(customerId).ifPresentOrElse(
                user -> {
                    // Set status to past_due - Stripe will retry payment
                    // We don't immediately downgrade, give grace period
                    user.setSubscriptionStatus("past_due");
                    userRepository.save(user);
                    log.warn("Payment failed for user {} - status set to past_due", user.getEmail());
                },
                () -> log.warn("No user found for customer: {}", customerId)
            );
        } catch (Exception e) {
            log.error("Failed to process invoice.payment_failed: {}", e.getMessage(), e);
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        log.info("Processing customer.subscription.updated event: {}", event.getId());

        var deserializer = event.getDataObjectDeserializer();
        String rawJson = deserializer.getRawJson();

        if (rawJson == null) {
            log.error("No raw JSON in event");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();
            String customerId = getJsonString(jsonObject, "customer");
            String status = getJsonString(jsonObject, "status");

            if (customerId == null) {
                log.error("No customer ID in subscription.updated event");
                return;
            }

            userRepository.findByStripeCustomerId(customerId).ifPresentOrElse(
                user -> {
                    String oldStatus = user.getSubscriptionStatus();
                    String oldRole = user.getRole();
                    user.setSubscriptionStatus(status);

                    // Handle status transitions
                    if ("canceled".equals(status) || "unpaid".equals(status)) {
                        user.setRole("ROLE_FREE");
                        log.info("User {} downgraded to ROLE_FREE (subscription status: {})",
                                 user.getEmail(), status);
                    } else if ("active".equals(status) && !"active".equals(oldStatus)) {
                        // Reactivation - keep existing role if it's a paid tier, otherwise default to STANDARD
                        if (!"ROLE_STANDARD".equals(oldRole) && !"ROLE_PRO".equals(oldRole)) {
                            user.setRole("ROLE_STANDARD");
                        }
                        log.info("User {} subscription reactivated (role: {})",
                                 user.getEmail(), user.getRole());
                    }

                    userRepository.save(user);
                    log.info("Subscription updated for user {} - status: {} -> {}",
                             user.getEmail(), oldStatus, status);
                },
                () -> log.warn("No user found for customer: {}", customerId)
            );
        } catch (Exception e) {
            log.error("Failed to process subscription.updated: {}", e.getMessage(), e);
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        log.info("Processing customer.subscription.deleted event: {}", event.getId());

        var deserializer = event.getDataObjectDeserializer();
        String rawJson = deserializer.getRawJson();

        if (rawJson == null) {
            log.error("No raw JSON in event");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();
            String customerId = getJsonString(jsonObject, "customer");

            if (customerId == null) {
                log.error("No customer ID in subscription.deleted event");
                return;
            }

            userRepository.findByStripeCustomerId(customerId).ifPresentOrElse(
                user -> {
                    user.setRole("ROLE_FREE");
                    user.setSubscriptionStatus("canceled");
                    user.setStripeSubscriptionId(null);
                    userRepository.save(user);
                    log.info("User {} downgraded to ROLE_FREE - subscription deleted", user.getEmail());
                },
                () -> log.warn("No user found for customer: {}", customerId)
            );
        } catch (Exception e) {
            log.error("Failed to process subscription.deleted: {}", e.getMessage(), e);
        }
    }

    // --- Helper methods ---

    private String extractEmail(JsonObject jsonObject) {
        // Try customer_email first
        String email = getJsonString(jsonObject, "customer_email");

        // Try metadata.user_email
        if (email == null && jsonObject.has("metadata") && !jsonObject.get("metadata").isJsonNull()) {
            JsonObject metadata = jsonObject.getAsJsonObject("metadata");
            email = getJsonString(metadata, "user_email");
        }

        // Try customer_details.email
        if (email == null && jsonObject.has("customer_details") && !jsonObject.get("customer_details").isJsonNull()) {
            JsonObject customerDetails = jsonObject.getAsJsonObject("customer_details");
            email = getJsonString(customerDetails, "email");
        }

        return email;
    }

    private String getJsonString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }
}
