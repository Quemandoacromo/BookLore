package com.adityachandel.booklore.controller;

import com.adityachandel.booklore.model.dto.AuthorDTO;
import com.adityachandel.booklore.service.AuthorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/author")
@RestController
@AllArgsConstructor
public class AuthorController {

    private AuthorService authorService;

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorDTO> getAuthor(@PathVariable long authorId) {
        return ResponseEntity.ok(authorService.getAuthorById(authorId));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<AuthorDTO>> getAuthorsByBookId(@PathVariable long bookId) {
        return ResponseEntity.ok(authorService.getAuthorsByBookId(bookId));
    }
}
