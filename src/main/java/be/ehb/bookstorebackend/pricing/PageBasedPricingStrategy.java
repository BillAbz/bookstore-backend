package be.ehb.bookstorebackend.pricing;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PageBasedPricingStrategy
        implements BookPricingStrategy {

    @Override
    public BigDecimal calculatePrice(int numberOfPages) {

        if (numberOfPages <= 0) {
            return new BigDecimal("19.99");
        }

        if (numberOfPages <= 150) {
            return new BigDecimal("12.99");
        }

        if (numberOfPages <= 300) {
            return new BigDecimal("19.99");
        }

        if (numberOfPages <= 500) {
            return new BigDecimal("29.99");
        }

        if (numberOfPages <= 800) {
            return new BigDecimal("39.99");
        }

        return new BigDecimal("49.99");
    }
}
