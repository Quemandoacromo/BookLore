package com.adityachandel.booklore.service.metadata;

import com.adityachandel.booklore.exception.ApiError;
import com.adityachandel.booklore.mapper.BookMapper;
import com.adityachandel.booklore.model.dto.Author;
import com.adityachandel.booklore.model.dto.Book;
import com.adityachandel.booklore.model.dto.request.BooksMetadataRefreshRequest;
import com.adityachandel.booklore.model.dto.request.LibraryMetadataRefreshRequest;
import com.adityachandel.booklore.model.entity.BookEntity;
import com.adityachandel.booklore.model.entity.BookMetadataEntity;
import com.adityachandel.booklore.model.entity.LibraryEntity;
import com.adityachandel.booklore.model.stomp.Topic;
import com.adityachandel.booklore.repository.BookRepository;
import com.adityachandel.booklore.repository.LibraryRepository;
import com.adityachandel.booklore.service.NotificationService;
import com.adityachandel.booklore.service.metadata.model.FetchMetadataRequest;
import com.adityachandel.booklore.service.metadata.model.FetchedBookMetadata;
import com.adityachandel.booklore.service.metadata.model.MetadataProvider;
import com.adityachandel.booklore.service.metadata.parser.AmazonBookParser;
import com.adityachandel.booklore.service.metadata.parser.GoodReadsParser;
import com.adityachandel.booklore.service.metadata.parser.GoogleParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.adityachandel.booklore.model.stomp.LogNotification.createLogNotification;

@Slf4j
@Service
@AllArgsConstructor
public class BookMetadataService {

    private final GoogleParser googleParser;
    private final AmazonBookParser amazonBookParser;
    private final GoodReadsParser goodReadsParser;
    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;
    private final BookMapper bookMapper;
    private final BookMetadataUpdater bookMetadataUpdater;
    private final NotificationService notificationService;

    public List<FetchedBookMetadata> fetchMetadataList(long bookId, FetchMetadataRequest request) {
        BookEntity bookEntity = bookRepository.findById(bookId).orElseThrow(() -> ApiError.BOOK_NOT_FOUND.createException(bookId));
        Book book = bookMapper.toBook(bookEntity);

        List<CompletableFuture<List<FetchedBookMetadata>>> futures = request.getProviders().stream()
                .map(provider -> CompletableFuture.supplyAsync(() -> fetchMetadataFromProvider(provider, book, request))
                        .exceptionally(e -> {
                            log.error("Error fetching metadata from provider: {}", provider, e);
                            return List.of();
                        }))
                .toList();

        List<List<FetchedBookMetadata>> allMetadata = futures.stream().map(CompletableFuture::join).toList();

        List<FetchedBookMetadata> interleavedMetadata = new ArrayList<>();
        int maxSize = allMetadata.stream().mapToInt(List::size).max().orElse(0);

        for (int i = 0; i < maxSize; i++) {
            for (List<FetchedBookMetadata> metadataList : allMetadata) {
                if (i < metadataList.size()) {
                    interleavedMetadata.add(metadataList.get(i));
                }
            }
        }

        return interleavedMetadata;
    }

    private List<FetchedBookMetadata> fetchMetadataFromProvider(MetadataProvider provider, Book book, FetchMetadataRequest request) {
        if (provider == MetadataProvider.AMAZON) {
            return amazonBookParser.fetchMetadata(book, request);
        } else if (provider == MetadataProvider.GOOD_READS) {
            return goodReadsParser.fetchMetadata(book, request);
        } else if (provider == MetadataProvider.GOOGLE) {
            return googleParser.fetchMetadata(book, request);
        } else {
            throw ApiError.METADATA_SOURCE_NOT_IMPLEMENT_OR_DOES_NOT_EXIST.createException();
        }
    }

    public FetchedBookMetadata fetchTopMetadata(MetadataProvider provider, Book book) {
        FetchMetadataRequest fetchMetadataRequest = FetchMetadataRequest.builder()
                .isbn(book.getMetadata().getIsbn10())
                .author(book.getMetadata().getAuthors().stream().map(Author::getName).collect(Collectors.joining(", ")))
                .title(book.getMetadata().getTitle())
                .bookId(book.getId())
                .build();
        if (provider == MetadataProvider.AMAZON) {
            return amazonBookParser.fetchTopMetadata(book, fetchMetadataRequest);
        } else if (provider == MetadataProvider.GOOD_READS) {
            return goodReadsParser.fetchTopMetadata(book, fetchMetadataRequest);
        } else if (provider == MetadataProvider.GOOGLE) {
            return goodReadsParser.fetchTopMetadata(book, fetchMetadataRequest);
        } else {
            throw ApiError.METADATA_SOURCE_NOT_IMPLEMENT_OR_DOES_NOT_EXIST.createException();
        }
    }

    @Transactional
    public void refreshLibraryMetadata(LibraryMetadataRefreshRequest request) {
        LibraryEntity libraryEntity = libraryRepository.findById(request.getLibraryId()).orElseThrow(() -> ApiError.LIBRARY_NOT_FOUND.createException(request.getLibraryId()));
        List<BookEntity> books = libraryEntity.getBookEntities().stream()
                .sorted(Comparator.comparing(BookEntity::getFileName, Comparator.nullsLast(String::compareTo)))
                .toList();
        refreshBooksMetadata(books, request.getMetadataProvider(), request.isReplaceCover());
        log.info("Library Refresh Metadata task completed!");
    }

    @Transactional
    public void refreshBooksMetadata(BooksMetadataRefreshRequest request) {
        List<BookEntity> books = bookRepository.findAllByIdIn(request.getBookIds()).stream()
                .sorted(Comparator.comparing(BookEntity::getFileName, Comparator.nullsLast(String::compareTo)))
                .toList();
        refreshBooksMetadata(books, request.getMetadataProvider(), request.isReplaceCover());
        log.info("Books Refresh Metadata task completed!");
    }

    @Transactional
    public void refreshBooksMetadata(List<BookEntity> books, MetadataProvider metadataProvider, boolean replaceCover) {
        try {
            for (BookEntity bookEntity : books) {
                FetchedBookMetadata metadata = fetchTopMetadata(metadataProvider, bookMapper.toBook(bookEntity));
                if (metadata != null) {
                    BookMetadataEntity bookMetadata = bookMetadataUpdater.setBookMetadata(bookEntity.getId(), metadata, metadataProvider, replaceCover);
                    bookEntity.setMetadata(bookMetadata);
                    bookRepository.save(bookEntity);

                    Book book = bookMapper.toBook(bookEntity);
                    notificationService.sendMessage(Topic.METADATA_UPDATE, book);
                    notificationService.sendMessage(Topic.LOG, createLogNotification("Book metadata updated: " + book.getMetadata().getTitle()));
                    if (metadataProvider == MetadataProvider.GOOD_READS) {
                        Thread.sleep(Duration.ofSeconds(ThreadLocalRandom.current().nextInt(3, 10)).toMillis());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while parsing library books", e);
        }
    }

}