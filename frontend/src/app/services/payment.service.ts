import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../config/app-config';
import { CheckoutSession } from '../models/order.model';

const PAYMENTS_API_URL = `${API_URL}/payments`;

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);

  // The backend expects the numeric order id as a string (despite the field name).
  createCheckoutSession(orderId: number): Observable<CheckoutSession> {
    return this.http.post<CheckoutSession>(`${PAYMENTS_API_URL}/create-checkout-session`, {
      orderNumber: String(orderId),
    });
  }
}
