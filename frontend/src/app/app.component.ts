import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatIconModule, MatButtonModule],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <mat-icon>verified</mat-icon>
      <span style="margin-left: 8px;">Quality Clearance Agent</span>
      <span class="spacer"></span>
      <button mat-button routerLink="/dashboard" routerLinkActive="active-link">
        <mat-icon>dashboard</mat-icon> Dashboard
      </button>
      <button mat-button routerLink="/clearance" routerLinkActive="active-link">
        <mat-icon>pending_actions</mat-icon> Clearance
      </button>
      <button mat-button routerLink="/defect-entry" routerLinkActive="active-link">
        <mat-icon>bug_report</mat-icon> Defect Entry
      </button>
      <button mat-button routerLink="/rework" routerLinkActive="active-link">
        <mat-icon>replay</mat-icon> Rework Orders
      </button>
    </mat-toolbar>
    <router-outlet></router-outlet>
  `,
  styles: [`.active-link { background-color: rgba(255,255,255,0.2); border-radius: 4px; }`]
})
export class AppComponent {}