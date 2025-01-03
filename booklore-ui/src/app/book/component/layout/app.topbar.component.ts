import {Component, ElementRef, OnDestroy, ViewChild} from '@angular/core';
import {MenuItem} from 'primeng/api';
import {LayoutService} from './service/app.layout.service';
import {RouterLink} from '@angular/router';
import {DialogService as PrimeDialogService, DynamicDialogRef} from 'primeng/dynamicdialog';
import {LibraryCreatorComponent} from '../library-creator/library-creator.component';
import {TooltipModule} from 'primeng/tooltip';
import {FormsModule} from '@angular/forms';
import {InputTextModule} from 'primeng/inputtext';
import {SearchComponent} from '../search/search.component';
import {FileUploadComponent} from '../file-upload/file-upload.component';
import {NgClass, NgIf} from '@angular/common';
import {EventService} from '../../service/event.service';
import {LogNotification} from '../../model/log-notification.model';
import {Button} from 'primeng/button';
import {AppConfiguratorComponent} from '../configurator/configurator.component';
import {StyleClass} from 'primeng/styleclass';
import {Divider} from 'primeng/divider';
import {BookInfoTabsComponent} from '../../../book-info-tabs/book-info-tabs.component';

@Component({
  selector: 'app-topbar',
  templateUrl: './app.topbar.component.html',
  styleUrls: ['./app.topbar.component.scss'],
  imports: [
    RouterLink,
    TooltipModule,
    FormsModule,
    InputTextModule,
    SearchComponent,
    NgIf,
    Button,
    AppConfiguratorComponent,
    StyleClass,
    NgClass,
    Divider
  ],
})
export class AppTopBarComponent implements OnDestroy {
  items!: MenuItem[];
  ref: DynamicDialogRef | undefined;

  latestEvent: LogNotification = {message: 'No recent notifications...'};
  eventHighlight: boolean = false;
  showEvents: boolean = false;
  eventTimeout: any;

  @ViewChild('menubutton') menuButton!: ElementRef;
  @ViewChild('topbarmenubutton') topbarMenuButton!: ElementRef;
  @ViewChild('topbarmenu') menu!: ElementRef;

  constructor(public layoutService: LayoutService, public dialogService: PrimeDialogService, private eventService: EventService) {
    this.eventService.latestEvent$.subscribe(event => {
      this.latestEvent = event;
    });
    this.eventService.eventHighlight$.subscribe(highlight => {
      this.eventHighlight = highlight;
    });
  }

  isMenuVisible: boolean = true;

  toggleMenu() {
    this.isMenuVisible = !this.isMenuVisible;
    this.layoutService.onMenuToggle();
  }

  onMouseEnter() {
    this.showEvents = true;
  }

  onMouseLeave() {
    this.showEvents = false;
  }

  toggleEventDisplay(): void {
    this.showEvents = !this.showEvents;
  }


  openBookDetailsDialog(): void {
    this.ref = this.dialogService.open(BookInfoTabsComponent, {
      header: 'Open book details',
      modal: true,
      closable: true,
      width: '1200px',
      height: '835px',
      showHeader: false,
      closeOnEscape: true,
      data: {
        bookId: 1
      }
    });
  }

  openLibraryCreatorDialog(): void {
    this.ref = this.dialogService.open(LibraryCreatorComponent, {
      header: 'Create New Library',
      modal: true,
      closable: true,
      width: '675px',
      height: '480px',
      style: {
        position: 'absolute',
        top: '15%',
      }
    });
  }

  openFileUploadDialog() {
    this.ref = this.dialogService.open(FileUploadComponent, {
      header: 'Upload Book',
      modal: true,
      closable: true,
      width: '600px',
      height: '330px',
      style: {
        position: 'absolute',
        top: '15%',
      }
    });
  }

  ngOnDestroy(): void {
    if (this.ref) {
      this.ref.close();
    }
    clearTimeout(this.eventTimeout);
  }
}
