package be.ehb.bookstorebackend.pricing;

import java.math.BigDecimal;

public interface BookPricingStrategy {

    BigDecimal calculatePrice(int numberOfPages);

}
