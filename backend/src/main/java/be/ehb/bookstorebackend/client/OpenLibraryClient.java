package be.ehb.bookstorebackend.client;

import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import be.ehb.bookstorebackend.exception.BookImportNotFoundException;
import org.springframework.web.client.HttpClientErrorException;
import be.ehb.bookstorebackend.exception.OpenLibraryUnavailableException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;

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
        try {
            return restClient.get()
                    .uri("/isbn/{isbn}.json", isbn)
                    .retrieve()
                    .body(JsonNode.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new BookImportNotFoundException(isbn);

        } catch (HttpServerErrorException ex) {
            throw new OpenLibraryUnavailableException();

        } catch (RestClientException ex) {
            throw new OpenLibraryUnavailableException();
        }
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


    public JsonNode getWorkByKey(String workKey) {

        if (workKey == null ||
                !workKey.matches("/works/OL[0-9]+W")) {
            throw new IllegalArgumentException(
                    "Invalid work key"
            );
        }

        try {
            return restClient.get()
                    .uri(workKey + ".json")
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientException ex) {
            return null;
        }
    }
}
