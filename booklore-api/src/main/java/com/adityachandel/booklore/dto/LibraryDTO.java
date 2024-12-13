package com.adityachandel.booklore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LibraryDTO {
    private Long id;
    private String name;
}

