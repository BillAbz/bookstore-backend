package be.ehb.bookstorebackend.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PageBasedPricingStrategyTest {

    private PageBasedPricingStrategy pricingStrategy;

    @BeforeEach
    void setUp() {
        pricingStrategy = new PageBasedPricingStrategy();
    }

    @ParameterizedTest
    @CsvSource({
            "1, 12.99",
            "150, 12.99",
            "151, 19.99",
            "300, 19.99",
            "301, 29.99",
            "500, 29.99",
            "501, 39.99",
            "800, 39.99",
            "801, 49.99",
            "1000, 49.99"
    })
    void shouldCalculatePriceBasedOnPageCount(
            int pages, String expectedPrice) {

        BigDecimal result =
                pricingStrategy.calculatePrice(pages);

        assertEquals(
                new BigDecimal(expectedPrice),
                result
        );
    }

    @Test
    void shouldUseDefaultPriceWhenPageCountUnknown() {

        BigDecimal result =
                pricingStrategy.calculatePrice(0);

        assertEquals(
                new BigDecimal("19.99"),
                result
        );
    }

    @Test
    void shouldUseDefaultPriceForNegativePageCount() {

        BigDecimal result =
                pricingStrategy.calculatePrice(-1);

        assertEquals(
                new BigDecimal("19.99"),
                result
        );
    }
}
