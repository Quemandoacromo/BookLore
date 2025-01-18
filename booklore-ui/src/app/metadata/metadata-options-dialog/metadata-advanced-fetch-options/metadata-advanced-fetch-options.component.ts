import {Component, Output, EventEmitter, inject, Input, OnInit, OnChanges, SimpleChanges} from '@angular/core';
import {Select, SelectChangeEvent} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {NgForOf, TitleCasePipe} from '@angular/common';
import {Checkbox} from 'primeng/checkbox';
import {Button} from 'primeng/button';
import {MessageService} from 'primeng/api';
import {FieldOptions, FieldProvider, MetadataRefreshOptions} from '../../model/request/metadata-refresh-options.model';

@Component({
  selector: 'app-metadata-advanced-fetch-options',
  templateUrl: './metadata-advanced-fetch-options.component.html',
  imports: [Select, FormsModule, NgForOf, Checkbox, Button, TitleCasePipe],
  styleUrl: './metadata-advanced-fetch-options.component.scss',
  standalone: true
})
export class MetadataAdvancedFetchOptionsComponent implements OnChanges {

  @Output() metadataOptionsSubmitted: EventEmitter<MetadataRefreshOptions> = new EventEmitter<MetadataRefreshOptions>();
  fields: (keyof FieldOptions)[] = ['title', 'description', 'authors', 'categories', 'cover'];
  providers: string[] = ['Amazon', 'Google', 'GoodReads'];
  refreshCovers: boolean = false;

  allDefault = {placeholder: 'Set All', value: ''};
  allP2 = {placeholder: 'Set All', value: ''};
  allP1 = {placeholder: 'Set All', value: ''};

  fieldOptions: FieldOptions = {
    title: {default: null, p2: null, p1: null},
    description: {default: null, p2: null, p1: null},
    authors: {default: null, p2: null, p1: null},
    categories: {default: null, p2: null, p1: null},
    cover: {default: null, p2: null, p1: null}
  };

  private messageService = inject(MessageService);
  @Input() currentMetadataOptions!: MetadataRefreshOptions;


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentMetadataOptions'] && this.currentMetadataOptions) {
      this.refreshCovers = this.currentMetadataOptions.refreshCovers || false;
      this.fieldOptions = this.currentMetadataOptions.fieldOptions || this.fieldOptions;
      this.allDefault.value = this.currentMetadataOptions.defaultProvider || '';
    }
  }

  syncProvider(event: SelectChangeEvent, providerType: keyof FieldProvider) {
    for (const field of Object.keys(this.fieldOptions)) {
      this.fieldOptions[field as keyof FieldOptions][providerType] = event.value;
    }
  }

  submit() {
    const allProvidersSelected = Object.keys(this.fieldOptions).every(field => {
      return this.fieldOptions[field as keyof FieldOptions].default !== null;
    });
    if (allProvidersSelected) {
      const metadataRefreshOptions: MetadataRefreshOptions = {
        defaultProvider: this.allDefault.value,
        refreshCovers: this.refreshCovers,
        fieldOptions: this.fieldOptions
      };

      this.metadataOptionsSubmitted.emit(metadataRefreshOptions);
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        life: 5000,
        detail: 'Base provider must be selected for all the book fields.'
      });
    }
  }

  reset() {
    this.allDefault.value = '';
    this.allP2.value = '';
    this.allP1.value = '';
    for (const field of Object.keys(this.fieldOptions)) {
      this.fieldOptions[field as keyof FieldOptions] = {
        default: null,
        p2: null,
        p1: null
      };
    }
  }

}
