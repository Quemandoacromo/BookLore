import {Component, inject, OnInit} from '@angular/core';
import {AppMenuitemComponent} from './app.menuitem.component';
import {AsyncPipe, NgForOf, NgIf} from '@angular/common';
import {MenuModule} from 'primeng/menu';
import {LibraryService} from '../../../book/service/library.service';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {ShelfService} from '../../../book/service/shelf.service';
import {BookService} from '../../../book/service/book.service';
import {LibraryShelfMenuService} from '../../../book/service/library-shelf-menu.service';

@Component({
  selector: 'app-menu',
  imports: [AppMenuitemComponent, NgIf, NgForOf, MenuModule, AsyncPipe],
  templateUrl: './app.menu.component.html',
})
export class AppMenuComponent implements OnInit {
  libraryMenu$: Observable<any> | undefined;
  shelfMenu$: Observable<any> | undefined;
  homeMenu$: Observable<any> | undefined;

  private libraryService = inject(LibraryService);
  private shelfService = inject(ShelfService);
  private bookService = inject(BookService);
  private libraryShelfMenuService = inject(LibraryShelfMenuService);


  ngOnInit(): void {
    this.libraryMenu$ = this.libraryService.libraryState$.pipe(
      map((state) => [
        {
          label: 'Library',
          separator: false,
          items: state.libraries?.map((library) => ({
            menu: this.libraryShelfMenuService.initializeLibraryMenuItems(library),
            label: library.name,
            type: 'Library',
            icon: 'pi pi-' + library.icon,
            routerLink: [`/library/${library.id}/books`],
            bookCount$: this.libraryService.getBookCount(library.id ?? 0),
          })) || [],
        },
      ])
    );

    this.shelfMenu$ = this.shelfService.shelfState$.pipe(
      map((state) => [
        {
          label: 'Shelves',
          separator: false,
          items: state.shelves?.map((shelf) => ({
            menu: this.libraryShelfMenuService.initializeLibraryMenuItems(shelf),
            label: shelf.name,
            type: 'Shelf',
            icon: 'pi pi-' + shelf.icon,
            routerLink: [`/shelf/${shelf.id}/books`],
            bookCount$: this.shelfService.getBookCount(shelf.id ?? 0),
          })) || [],
        },
      ])
    );

    this.homeMenu$ = this.bookService.bookState$.pipe(
      map((bookState) => {
        return [
          {
            label: 'Home',
            separator: false,
            items: [
              {
                label: 'Dashboard',
                icon: 'pi pi-fw pi-home',
                routerLink: ['/'],
              },
              {
                label: 'All Books',
                type: 'All Books',
                icon: 'pi pi-fw pi-book',
                routerLink: ['/all-books'],
                bookCount$: of(bookState.books ? bookState.books.length : 0),
              },
            ],
          },
        ];
      })
    );
  }

}
