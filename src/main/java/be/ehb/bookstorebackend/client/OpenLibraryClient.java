package be.ehb.bookstorebackend.client;

import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenLibraryClient {

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://openlibrary.org")
                .defaultHeader(
                        "User-Agent",
                        "BookstoreBackend/1.0 (educational project)"
                )
                .build();
    }

    public JsonNode getBookByIsbn(String isbn) {
        return restClient.get()
                .uri("/isbn/{isbn}.json", isbn)
                .retrieve()
                .body(JsonNode.class);
    }

    public String getAuthorName(String authorKey) {

        if (authorKey == null ||
                !authorKey.matches("/authors/OL[0-9]+A")) {
            throw new IllegalArgumentException(
                    "Invalid author key"
            );
        }

        JsonNode authorJson = restClient.get()
                .uri(authorKey + ".json")
                .retrieve()
                .body(JsonNode.class);

        if (authorJson == null) {
            return "Unknown author";
        }

        return authorJson.path("name")
                .asText("Unknown author");
    }

}
