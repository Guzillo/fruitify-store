import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  AuthenticationResponse,
  SignupRequest,
  UpdatePasswordRequest,
  UpdateUserRequest,
  UserProfile,
} from '../models/auth.model';

const API_URL = 'http://localhost:8080/api';
const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly currentUser = signal<UserProfile | null>(null);
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  // Only drives which controls are shown; the server checks the role on every admin call.
  readonly isAdmin = signal(hasAdminRole(localStorage.getItem(ACCESS_TOKEN_KEY)));

  signup(request: SignupRequest): Observable<void> {
    return this.http.post<void>(`${API_URL}/auth/signup`, request);
  }

  // Sign-in uses HTTP Basic once; the response carries the JWT we use from now on.
  signin(email: string, password: string): Observable<AuthenticationResponse> {
    const headers = new HttpHeaders({ Authorization: `Basic ${encodeBasic(email, password)}` });
    return this.http
      .post<AuthenticationResponse>(`${API_URL}/auth/signin`, null, { headers })
      .pipe(tap((tokens) => this.storeTokens(tokens)));
  }

  refresh(): Observable<AuthenticationResponse> {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY) ?? '';
    return this.http
      .post<AuthenticationResponse>(`${API_URL}/auth/refresh`, { refreshToken })
      .pipe(tap((tokens) => this.storeTokens(tokens)));
  }

  loadCurrentUser(): Observable<UserProfile> {
    return this.http
      .get<UserProfile>(`${API_URL}/users/me`)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  updateProfile(request: UpdateUserRequest): Observable<UserProfile> {
    return this.http
      .put<UserProfile>(`${API_URL}/users/me`, request)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  changePassword(request: UpdatePasswordRequest): Observable<void> {
    return this.http.put<void>(`${API_URL}/users/me/password`, request);
  }

  logout(): void {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (refreshToken) {
      // Revoke on the server; if it fails the token is already unusable, so ignore the error.
      this.http.post(`${API_URL}/auth/logout`, { refreshToken }).subscribe({
        error: () => undefined,
      });
    }
    this.clear();
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.currentUser.set(null);
    this.isAdmin.set(false);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  private storeTokens(tokens: AuthenticationResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    this.isAdmin.set(hasAdminRole(tokens.accessToken));
  }
}

// Reads the "scope" claim of the JWT (roles are listed there as ROLE_ADMIN, ROLE_USER, ...).
// The signature is not checked here; that is the server's job.
function hasAdminRole(token: string | null): boolean {
  if (!token) {
    return false;
  }
  try {
    const payloadPart = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(payloadPart));
    return typeof payload.scope === 'string' && payload.scope.split(' ').includes('ROLE_ADMIN');
  } catch {
    return false;
  }
}

function encodeBasic(email: string, password: string): string {
  const bytes = new TextEncoder().encode(`${email}:${password}`);
  return btoa(String.fromCharCode(...bytes));
}
