import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ClearanceComponent } from './pages/clearance/clearance.component';
import { DefectEntryComponent } from './pages/defect-entry/defect-entry.component';
import { ReworkComponent } from './pages/rework/rework.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'clearance', component: ClearanceComponent },
  { path: 'defect-entry', component: DefectEntryComponent },
  { path: 'rework', component: ReworkComponent }
];