package com.adityachandel.booklore.service.fileprocessor;

import com.adityachandel.booklore.mapper.BookMapper;
import com.adityachandel.booklore.model.dto.settings.LibraryFile;
import com.adityachandel.booklore.model.dto.Book;
import com.adityachandel.booklore.model.entity.BookEntity;
import com.adityachandel.booklore.model.entity.BookMetadataEntity;
import com.adityachandel.booklore.model.enums.BookFileType;
import com.adityachandel.booklore.repository.BookRepository;
import com.adityachandel.booklore.service.BookCreatorService;
import com.adityachandel.booklore.util.FileUtils;
import io.documentnode.epub4j.domain.Identifier;
import io.documentnode.epub4j.domain.Metadata;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EpubProcessor implements FileProcessor {

    private final BookRepository bookRepository;
    private final BookCreatorService bookCreatorService;
    private final BookMapper bookMapper;
    private final FileProcessingUtils fileProcessingUtils;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Book processFile(LibraryFile libraryFile, boolean forceProcess) {
        File bookFile = new File(libraryFile.getFileName());
        String fileName = bookFile.getName();
        if (!forceProcess) {
            Optional<BookEntity> bookOptional = bookRepository.findBookByFileNameAndLibraryId(fileName, libraryFile.getLibraryEntity().getId());
            return bookOptional
                    .map(bookMapper::toBook)
                    .orElseGet(() -> processNewFile(libraryFile));
        } else {
            return processNewFile(libraryFile);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Book processNewFile(LibraryFile libraryFile) {
        BookEntity bookEntity = bookCreatorService.createShellBook(libraryFile, BookFileType.EPUB);
        try {
            io.documentnode.epub4j.domain.Book epub = new EpubReader().readEpub(new FileInputStream(FileUtils.getBookFullPath(bookEntity)));

            setBookMetadata(epub, bookEntity);
            processCover(epub, bookEntity);

            bookCreatorService.saveConnections(bookEntity);
            bookRepository.save(bookEntity);
            bookRepository.flush();

        } catch (Exception e) {
            log.error("Error while processing file {}, error: {}", libraryFile.getFileName(), e.getMessage());
        }
        return bookMapper.toBook(bookEntity);
    }

    private void processCover(io.documentnode.epub4j.domain.Book epub, BookEntity bookEntity) throws IOException {
        Resource coverImage = epub.getCoverImage();
        if (coverImage != null) {
            boolean success = saveCoverImage(coverImage, bookEntity.getId());
            if (success) {
                fileProcessingUtils.setBookCoverPath(bookEntity.getId(), bookEntity.getMetadata());
            }
        }
    }

    private static Set<String> getAuthors(io.documentnode.epub4j.domain.Book book) {
        return book.getMetadata().getAuthors().stream()
                .map(author -> author.getFirstname() + " " + author.getLastname())
                .collect(Collectors.toSet());
    }

    private void setBookMetadata(io.documentnode.epub4j.domain.Book book, BookEntity bookEntity) {
        BookMetadataEntity bookMetadata = bookEntity.getMetadata();
        Metadata epubMetadata = book.getMetadata();

        if (epubMetadata != null) {
            bookMetadata.setTitle(epubMetadata.getFirstTitle());

            if (epubMetadata.getDescriptions() != null && !epubMetadata.getDescriptions().isEmpty()) {
                bookMetadata.setDescription(epubMetadata.getDescriptions().getFirst());
            }

            if (epubMetadata.getPublishers() != null && !epubMetadata.getPublishers().isEmpty()) {
                bookMetadata.setPublisher(epubMetadata.getPublishers().getFirst());
            }

            List<String> identifiers = epubMetadata.getIdentifiers().stream()
                    .map(Identifier::getValue)
                    .toList();
            if (!identifiers.isEmpty()) {
                String isbn13 = identifiers.stream().filter(id -> id.length() == 13).findFirst().orElse(null);
                String isbn10 = identifiers.stream().filter(id -> id.length() == 10).findFirst().orElse(null);
                bookMetadata.setIsbn13(isbn13);
                bookMetadata.setIsbn10(isbn10);
            }

            bookMetadata.setLanguage(epubMetadata.getLanguage() == null || epubMetadata.getLanguage().equalsIgnoreCase("UND") ? "en" : epubMetadata.getLanguage());

            if (epubMetadata.getDates() != null && !epubMetadata.getDates().isEmpty()) {
                epubMetadata.getDates().stream()
                        .findFirst()
                        .ifPresent(publishedDate -> {
                            String dateString = publishedDate.getValue();
                            if (isValidLocalDate(dateString)) {
                                LocalDate parsedDate = LocalDate.parse(dateString);
                                bookMetadata.setPublishedDate(parsedDate);
                            } else if (isValidOffsetDateTime(dateString)) {
                                OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
                                bookMetadata.setPublishedDate(offsetDateTime.toLocalDate());
                            } else {
                                log.error("Unable to parse date: {}", dateString);
                            }
                        });
            }

            bookCreatorService.addAuthorsToBook(getAuthors(book), bookEntity);
            bookCreatorService.addCategoriesToBook(epubMetadata.getSubjects(), bookEntity);
        }
    }

    private boolean saveCoverImage(Resource coverImage, long bookId) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(coverImage.getData()));
        return fileProcessingUtils.saveCoverImage(originalImage, bookId);
    }

    private boolean isValidLocalDate(String dateString) {
        try {
            LocalDate.parse(dateString);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidOffsetDateTime(String dateString) {
        try {
            OffsetDateTime.parse(dateString);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}