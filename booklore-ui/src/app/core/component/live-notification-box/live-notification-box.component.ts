import {Component, inject, Input} from '@angular/core';
import {LogNotification} from '../../../shared/websocket/model/log-notification.model';
import {EventService} from '../../../shared/websocket/event.service';

@Component({
  selector: 'app-live-notification-box',
  standalone: true,
  templateUrl: './live-notification-box.component.html',
  styleUrl: './live-notification-box.component.scss',
  host: {
    class: 'config-panel'
  },
})
export class LiveNotificationBoxComponent {

  latestEvent: LogNotification = {message: 'No recent notifications...'};

  private eventService = inject(EventService);

  constructor() {
    this.eventService.latestEvent$.subscribe(event => {
      this.latestEvent = event;
    });
  }
}
