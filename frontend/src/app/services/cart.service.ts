import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../config/app-config';
import { Cart, CartItem } from '../models/cart.model';

const CART_API_URL = `${API_URL}/cart`;

// Every call needs a logged-in user; the auth interceptor adds the JWT.
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);

  getCart(): Observable<Cart> {
    return this.http.get<Cart>(CART_API_URL);
  }

  addItem(productId: number, quantity = 1): Observable<CartItem> {
    return this.http.post<CartItem>(`${CART_API_URL}/items`, { productId, quantity });
  }

  updateQuantity(itemId: number, quantity: number): Observable<CartItem> {
    return this.http.put<CartItem>(`${CART_API_URL}/items/${itemId}`, { quantity });
  }

  removeItem(itemId: number): Observable<void> {
    return this.http.delete<void>(`${CART_API_URL}/items/${itemId}`);
  }

  clear(): Observable<void> {
    return this.http.delete<void>(CART_API_URL);
  }
}
