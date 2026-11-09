package be.ehb.bookstorebackend.exception;

public class OpenLibraryUnavailableException extends RuntimeException {

    public OpenLibraryUnavailableException() {
        super("Open Library service is temporarily unavailable");
    }
}
