package com.adityachandel.booklore.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Author not found with ID: %d"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "Book not found with ID: %d"),
    FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading files from path"),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Image not found or not readable"),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "Invalid file format"),
    LIBRARY_NOT_FOUND(HttpStatus.NOT_FOUND, "Library not found with ID: %d"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    INVALID_QUERY_PARAMETERS(HttpStatus.BAD_REQUEST, "Query parameters are required for the search.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public APIException createException(Object... details) {
        String formattedMessage = (details.length > 0) ? String.format(message, details) : message;
        return new APIException(formattedMessage, this.status);
    }
}
