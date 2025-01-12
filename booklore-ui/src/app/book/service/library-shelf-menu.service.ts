import {Injectable} from '@angular/core';
import {ConfirmationService, MenuItem, MessageService} from 'primeng/api';
import {Router} from '@angular/router';
import {LibraryService} from './library.service';
import {ShelfService} from './shelf.service';
import {Library} from '../model/library.model';
import {Shelf} from '../model/shelf.model';
import {DialogService} from 'primeng/dynamicdialog';
import {MetadataFetchOptionsComponent} from '../../metadata/metadata-options-dialog/metadata-fetch-options/metadata-fetch-options.component';
import {MetadataRefreshType} from '../../metadata/model/request/metadata-refresh-type.enum';

@Injectable({
  providedIn: 'root',
})
export class LibraryShelfMenuService {
  constructor(
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private libraryService: LibraryService,
    private shelfService: ShelfService,
    private router: Router,
    private dialogService: DialogService
  ) {
  }

  initializeLibraryMenuItems(entity: Library | Shelf | null): MenuItem[] {
    return [
      {
        label: 'Options',
        items: [
          {
            label: 'Delete Library',
            icon: 'pi pi-trash',
            command: () => {
              this.confirmationService.confirm({
                message: `Are you sure you want to delete library: ${entity?.name}?`,
                header: 'Confirmation',
                rejectButtonProps: {
                  label: 'Cancel',
                },
                accept: () => {
                  this.libraryService.deleteLibrary(entity?.id!).subscribe({
                    complete: () => {
                      this.router.navigate(['/']);
                      this.messageService.add({severity: 'info', summary: 'Success', detail: 'Library was deleted'});
                    },
                    error: () => {
                      this.messageService.add({
                        severity: 'error',
                        summary: 'Failed',
                        detail: 'Failed to delete library',
                      });
                    }
                  });
                }
              });
            }
          },
          {
            label: 'Refresh Library',
            icon: 'pi pi-refresh',
            command: () => {
              this.confirmationService.confirm({
                message: `Are you sure you want to refresh library: ${entity?.name}?`,
                header: 'Confirmation',
                rejectButtonProps: {
                  label: 'Cancel',
                },
                accept: () => {
                  this.libraryService.refreshLibrary(entity?.id!).subscribe({
                    complete: () => {
                      this.messageService.add({severity: 'info', summary: 'Success', detail: 'Library refresh scheduled'});
                    },
                    error: () => {
                      this.messageService.add({
                        severity: 'error',
                        summary: 'Failed',
                        detail: 'Failed to refresh library',
                      });
                    }
                  });
                }
              });
            }
          },
          {
            label: 'Refresh Books Metadata',
            icon: 'pi pi-database',
            command: () => {
              this.dialogService.open(MetadataFetchOptionsComponent, {
                header: 'Metadata Refresh Options',
                modal: true,
                closable: true,
                data: {
                  libraryId: entity?.id,
                  metadataRefreshType: MetadataRefreshType.LIBRARY
                }
              })
            }
          }
        ]
      }
    ];
  }

  initializeShelfMenuItems(entity: any): MenuItem[] {
    return [
      {
        label: 'Options',
        items: [
          {
            label: 'Delete Shelf',
            icon: 'pi pi-trash',
            command: () => {
              this.confirmationService.confirm({
                message: `Are you sure you want to delete shelf: ${entity?.name}?`,
                header: 'Confirmation',
                accept: () => {
                  this.shelfService.deleteShelf(entity?.id!).subscribe({
                    complete: () => {
                      this.router.navigate(['/']);
                      this.messageService.add({severity: 'info', summary: 'Success', detail: 'Shelf was deleted'});
                    },
                    error: () => {
                      this.messageService.add({
                        severity: 'error',
                        summary: 'Failed',
                        detail: 'Failed to delete shelf',
                      });
                    }
                  });
                }
              });
            }
          }
        ]
      }
    ];
  }
}
