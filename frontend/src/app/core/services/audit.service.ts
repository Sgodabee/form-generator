import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/audit-log.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuditService {

  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Returns all audit log entries, newest first. */
  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/audit`);
  }
}
