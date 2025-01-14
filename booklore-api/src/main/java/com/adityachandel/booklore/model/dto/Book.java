package com.adityachandel.booklore.model.dto;

import com.adityachandel.booklore.model.enums.BookFileType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {
    private Long id;
    private BookFileType bookType;
    private Long libraryId;
    private String fileName;
    private String title;
    private Instant lastReadTime;
    private Instant addedOn;
    private BookMetadata metadata;
    private Integer pdfProgress;
    private String epubProgress;
    private List<Shelf> shelves;
}
