package com.Aga.Agali.service;


import com.Aga.Agali.entity.CvData;
import com.Aga.Agali.entity.Order;
import com.Aga.Agali.entity.User;
import com.Aga.Agali.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Value("${cv.price}")
    private String cvPrice;

    private final OrderRepository orderRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createPaymentLink(User user, CvData cvData) {
        try {
            long priceInCents = (long) (Double.parseDouble(cvPrice) * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("azn")
                                                    .setUnitAmount(priceInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("CV Hazırlanması")
                                                                    .setDescription("Professional CV - cv_az_bot")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("userId", user.getId().toString())
                    .putMetadata("cvDataId", cvData.getId().toString())
                    .build();

            Session session = Session.create(params);

            Order order = Order.builder()
                    .user(user)
                    .cvData(cvData)
                    .stripeSessionId(session.getId())
                    .paid(false)
                    .build();
            orderRepository.save(order);

            log.info("Stripe session yaradıldı: {}", session.getId());
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Stripe xətası: {}", e.getMessage());
            throw new RuntimeException("Ödəniş linki yaradıla bilmədi.");
        }
    }

    public boolean verifyPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return "paid".equals(session.getPaymentStatus());
        } catch (StripeException e) {
            log.error("Stripe yoxlama xətası: {}", e.getMessage());
            return false;
        }
    }
}