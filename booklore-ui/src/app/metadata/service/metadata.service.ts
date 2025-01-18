import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from "rxjs";
import {FetchMetadataRequest} from "../model/request/fetch-metadata-request.model";
import {BookMetadata, FetchedMetadata} from "../../book/model/book.model";
import {catchError, map, tap} from "rxjs/operators";
import {BookService} from "../../book/service/book.service";
import {MetadataRefreshRequest} from '../model/request/metadata-refresh-request.model';
import {MessageService} from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class MetadataService {

  private readonly url = 'http://localhost:8080/v1/metadata';

  private http = inject(HttpClient);
  private bookService = inject(BookService);
  private messageService = inject(MessageService);

  fetchBookMetadata(bookId: number, request: FetchMetadataRequest): Observable<FetchedMetadata[]> {
    return this.http.post<FetchedMetadata[]>(`${this.url}/${bookId}`, request);
  }

  updateBookMetadata(bookId: number, bookMetadata: BookMetadata): Observable<BookMetadata> {
    return this.http.put<BookMetadata>(`${this.url}/${bookId}`, bookMetadata).pipe(
      tap(updatedMetadata => {
        this.bookService.handleBookMetadataUpdate(bookId, updatedMetadata);
      })
    );
  }

  autoRefreshMetadata(metadataRefreshRequest: MetadataRefreshRequest): Observable<any> {
    return this.http.put<void>(`${this.url}/refreshV2`, metadataRefreshRequest).pipe(
      map(() => {
        this.messageService.add({
          severity: 'success',
          summary: 'Metadata Update Scheduled',
          detail: 'The metadata update for the selected books has been successfully scheduled.'
        });
        return {success: true};
      }),
      catchError((e) => {
        if (e.status === 409) {
          this.messageService.add({
            severity: 'error',
            summary: 'Task Already Running',
            life: 5000,
            detail: 'A metadata refresh task is already in progress. Please wait for it to complete before starting another one.'
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Metadata Update Failed',
            life: 5000,
            detail: 'An unexpected error occurred while scheduling the metadata update. Please try again later or contact support if the issue persists.'
          });
        }
        return of({success: false});
      })
    );
  }
}
