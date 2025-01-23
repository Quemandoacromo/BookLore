package com.adityachandel.booklore.controller;

import com.adityachandel.booklore.service.PathService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/path")
@AllArgsConstructor
public class PathController {

    private PathService pathService;

    @GetMapping
    public List<String> getFolders(@RequestParam String path) {
        return pathService.getFoldersAtPath(path);
    }
}
