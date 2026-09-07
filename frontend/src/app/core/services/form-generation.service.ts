import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GenerationResult } from '../models/generation-result.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FormGenerationService {

  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Triggers the CSV-to-PDF pipeline on the backend. */
  generate(): Observable<GenerationResult> {
    return this.http.post<GenerationResult>(`${this.baseUrl}/forms/generate`, {});
  }

  /** Returns the list of generated PDF file names from the local store. */
  listFiles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/files`);
  }
}
