package com.adityachandel.booklore.model.entity;

import com.adityachandel.booklore.convertor.PathsConverter;
import com.adityachandel.booklore.convertor.SortConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "library")
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Convert(converter = SortConverter.class)
    private Sort sort;

    @Convert(converter = PathsConverter.class)
    @Column(name = "paths")
    private List<String> paths;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books;
}
