package be.ehb.bookstorebackend.adapter;

import be.ehb.bookstorebackend.client.OpenLibraryClient;
import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.pricing.BookPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenLibraryBookAdapterTest {

    private OpenLibraryBookAdapter adapter;
    private BookPricingStrategy pricingStrategy;
    private OpenLibraryClient openLibraryClient;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        pricingStrategy = mock(BookPricingStrategy.class);
        openLibraryClient = mock(OpenLibraryClient.class);

        adapter = new OpenLibraryBookAdapter(
                pricingStrategy,
                openLibraryClient
        );

        jsonMapper = new JsonMapper();
    }

    @Test
    void shouldConvertOpenLibraryJsonToBook() throws Exception {

        String json = """
                {
                  "title": "The Great Gatsby",
                  "number_of_pages": 180,
                  "description": "A classic novel",
                  "authors": [
                    {"key": "/authors/OL27349A"}
                  ]
                }
                """;

        JsonNode bookJson = jsonMapper.readTree(json);

        when(pricingStrategy.calculatePrice(180))
                .thenReturn(new BigDecimal("19.99"));

        when(openLibraryClient.getAuthorName("/authors/OL27349A"))
                .thenReturn("F. Scott Fitzgerald");

        Book result = adapter.toBook(
                "9780743273565", bookJson
        );

        assertEquals("9780743273565", result.getIsbn());
        assertEquals("The Great Gatsby", result.getTitle());
        assertEquals("F. Scott Fitzgerald", result.getAuthor());
        assertEquals("A classic novel", result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(5, result.getStock());

        verify(pricingStrategy).calculatePrice(180);
        verify(openLibraryClient)
                .getAuthorName("/authors/OL27349A");
    }

    @Test
    void shouldUseDefaultValuesForMissingOptionalFields()
            throws Exception {

        String json = """
                {
                  "title": "Unknown Book"
                }
                """;

        JsonNode bookJson = jsonMapper.readTree(json);

        when(pricingStrategy.calculatePrice(0))
                .thenReturn(new BigDecimal("19.99"));

        Book result = adapter.toBook("1234567890", bookJson);

        assertEquals("Unknown Book", result.getTitle());
        assertEquals("Unknown author", result.getAuthor());
        assertEquals("No description available",
                result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());

        verifyNoInteractions(openLibraryClient);
    }

    @Test
    void shouldRejectBookWithoutTitle() throws Exception {

        String json = """
                {
                  "number_of_pages": 250
                }
                """;

        JsonNode bookJson = jsonMapper.readTree(json);

        assertThrows(IllegalArgumentException.class,
                () -> adapter.toBook("1234567890", bookJson));

        verifyNoInteractions(pricingStrategy, openLibraryClient);
    }

    @Test
    void shouldReadDescriptionFromObject() throws Exception {

        String json = """
                {
                  "title": "Example Book",
                  "description": {
                    "type": "/type/text",
                    "value": "Description stored as an object"
                  }
                }
                """;

        JsonNode bookJson = jsonMapper.readTree(json);

        when(pricingStrategy.calculatePrice(0))
                .thenReturn(new BigDecimal("19.99"));

        Book result = adapter.toBook("1234567890", bookJson);

        assertEquals(
                "Description stored as an object",
                result.getDescription()
        );
    }
}
