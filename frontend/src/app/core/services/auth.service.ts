import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, catchError, throwError, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly baseUrl = environment.apiUrl;

  /** Emits the current username, or null when logged out. */
  private currentUserSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('username')
  );
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Sends HTTP Basic credentials to the backend.
   * On success, stores the base64 token and username in localStorage.
   */
  login(username: string, password: string): Observable<{ username: string }> {
    const token = btoa(`${username}:${password}`);
    const headers = new HttpHeaders({ Authorization: `Basic ${token}` });

    return this.http.get<{ username: string }>(`${this.baseUrl}/auth/me`, { headers }).pipe(
      tap(response => {
        localStorage.setItem('authToken', token);
        localStorage.setItem('username', response.username);
        this.currentUserSubject.next(response.username);
      }),
      catchError(err => {
        this.clearSession();
        return throwError(() => new Error('Invalid username or password.'));
      })
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/logout`, {}).pipe(
      tap(() => this.clearSession()),
      catchError(() => {
        this.clearSession();
        return throwError(() => new Error('Logout failed.'));
      })
    );
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('authToken');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getAuthToken(): string | null {
    return localStorage.getItem('authToken');
  }

  private clearSession(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    this.currentUserSubject.next(null);
  }
}
