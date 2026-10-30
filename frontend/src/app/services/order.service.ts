import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminOrderPage, Order, OrderPage, ShippingAddress } from '../models/order.model';

const API_URL = 'http://localhost:8080/api/orders';
const ADMIN_API_URL = 'http://localhost:8080/api/admin/orders';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);

  // Creates a PENDING order from the current cart and empties the cart.
  createOrder(shippingAddress: ShippingAddress): Observable<Order> {
    return this.http.post<Order>(API_URL, { shippingAddress });
  }

  getOrders(page = 0, size = 10): Observable<OrderPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<OrderPage>(API_URL, { params });
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(`${API_URL}/${id}`);
  }

  // Admin only: every customer's orders.
  getAllOrders(page = 0, size = 10): Observable<AdminOrderPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<AdminOrderPage>(ADMIN_API_URL, { params });
  }

  // Admin only. PENDING -> PAID, PENDING/PAID -> CANCELLED (cancelling restores stock).
  changeStatus(id: number, status: 'PAID' | 'CANCELLED'): Observable<Order> {
    return this.http.patch<Order>(`${ADMIN_API_URL}/${id}/status`, { status });
  }

  // Only unpaid orders can be cancelled; the server closes the Stripe session and releases stock.
  cancelOrder(id: number): Observable<Order> {
    return this.http.post<Order>(`${API_URL}/${id}/cancel`, null);
  }
}
