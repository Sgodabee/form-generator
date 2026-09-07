import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * Functional HTTP interceptor (Angular 17+ style).
 * Attaches the stored Basic auth token to every outgoing request,
 * and redirects to login on 401 responses.
 */
export const authInterceptorFn: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('authToken');

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Basic ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
