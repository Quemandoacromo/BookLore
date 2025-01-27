package com.adityachandel.booklore.model.dto;

import com.adityachandel.booklore.model.enums.MetadataProvider;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BookMetadata {
    // Identifiers
    private Long bookId;
    private MetadataProvider provider;
    private String providerBookId;
    private String thumbnailUrl;

    // Book Information
    private String title;
    private String subtitle;
    private String publisher;
    private LocalDate publishedDate;
    private String description;
    private String seriesName;
    private Integer seriesNumber;
    private Integer seriesTotal;
    private String isbn13;
    private String isbn10;
    private Integer pageCount;
    private String language;

    // Rating & Reviews
    private Double rating;
    private Integer ratingCount;
    private Integer reviewCount;

    // Cover Information
    private Instant coverUpdatedOn;

    // Content
    private List<String> authors;
    private List<String> categories;
    private List<Award> awards;

    // Locking Information
    private Boolean allFieldsLocked;
    private Boolean titleLocked;
    private Boolean subtitleLocked;
    private Boolean publisherLocked;
    private Boolean publishedDateLocked;
    private Boolean descriptionLocked;
    private Boolean seriesNameLocked;
    private Boolean seriesNumberLocked;
    private Boolean seriesTotalLocked;
    private Boolean isbn13Locked;
    private Boolean isbn10Locked;
    private Boolean pageCountLocked;
    private Boolean languageLocked;
    private Boolean ratingLocked;
    private Boolean reviewCountLocked;
    private Boolean coverLocked;
    private Boolean authorsLocked;
    private Boolean categoriesLocked;
}