package com.Aga.Agali.controller;

import com.Aga.Agali.entity.Order;
import com.Aga.Agali.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final OrderRepository orderRepository;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook imza xətası: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (session == null) {
                log.error("Session deserialize edilə bilmədi");
                return ResponseEntity.ok("Session null");
            }

            String sessionId = session.getId();
            Optional<Order> orderOpt = orderRepository.findByStripeSessionId(sessionId);

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setPaid(true);
                order.setPaidAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Ödəniş təsdiqləndi, session: {}", sessionId);
            } else {
                log.warn("Order tapılmadı, session: {}", sessionId);
            }
        }

        return ResponseEntity.ok("OK");
    }
}