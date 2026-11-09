package be.ehb.bookstorebackend.adapter;

import be.ehb.bookstorebackend.entity.Book;
import be.ehb.bookstorebackend.pricing.BookPricingStrategy;
import be.ehb.bookstorebackend.client.OpenLibraryClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import java.math.BigDecimal;

@Component
public class OpenLibraryBookAdapter {

    private final BookPricingStrategy pricingStrategy;
    private final OpenLibraryClient openLibraryClient;

    public OpenLibraryBookAdapter(
            BookPricingStrategy pricingStrategy,
            OpenLibraryClient openLibraryClient) {

        this.pricingStrategy = pricingStrategy;
        this.openLibraryClient = openLibraryClient;
    }


    public Book toBook(String isbn, JsonNode json) {

        if (json == null || json.isNull()) {
            throw new IllegalArgumentException(
                    "Book data is missing");
        }

        String title = json.path("title").asText("");

        if (title.isBlank()) {
            throw new IllegalArgumentException(
                    "Book title is missing");
        }

        int numberOfPages =
                json.path("number_of_pages").asInt(0);

        BigDecimal price =
                pricingStrategy.calculatePrice(numberOfPages);

        String description = "No description available";

        JsonNode descriptionNode = json.path("description");

        if (descriptionNode.isTextual()) {
            description = descriptionNode.asText();
        } else if (descriptionNode.isObject()) {
            description = descriptionNode
                    .path("value")
                    .asText(description);
        }


        String author = "Unknown author";

        JsonNode authors = json.path("authors");

        if (authors.isArray() && !authors.isEmpty()) {

            JsonNode firstAuthor = authors.get(0);

            String authorKey = firstAuthor
                    .path("key")
                    .asText("");

            if (authorKey.isBlank()) {
                authorKey = firstAuthor
                        .path("author")
                        .path("key")
                        .asText("");
            }

            if (authorKey.matches("/authors/OL[0-9]+A")) {
                author = openLibraryClient.getAuthorName(authorKey);
            }
        }


        return new Book(
                isbn,
                title,
                author,
                description,
                price,
                5
        );
    }
}
