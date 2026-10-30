import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cart, CartItem } from '../models/cart.model';

const API_URL = 'http://localhost:8080/api/cart';

// Every call needs a logged-in user; the auth interceptor adds the JWT.
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);

  getCart(): Observable<Cart> {
    return this.http.get<Cart>(API_URL);
  }

  addItem(productId: number, quantity = 1): Observable<CartItem> {
    return this.http.post<CartItem>(`${API_URL}/items`, { productId, quantity });
  }

  updateQuantity(itemId: number, quantity: number): Observable<CartItem> {
    return this.http.put<CartItem>(`${API_URL}/items/${itemId}`, { quantity });
  }

  removeItem(itemId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/items/${itemId}`);
  }

  clear(): Observable<void> {
    return this.http.delete<void>(API_URL);
  }
}
