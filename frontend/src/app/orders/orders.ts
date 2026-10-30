import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminOrderSummary, OrderSummary } from '../models/order.model';
import { AuthService } from '../services/auth.service';
import { OrderService } from '../services/order.service';
import { LoadingState } from '../shared/loading-state/loading-state';

type Scope = 'mine' | 'all';

@Component({
  selector: 'app-orders',
  imports: [CurrencyPipe, DatePipe, RouterLink, LoadingState],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders implements OnInit {
  private readonly orderService = inject(OrderService);
  protected readonly auth = inject(AuthService);

  // Admins can switch between their own orders and every customer's orders.
  readonly scope = signal<Scope>('mine');
  readonly orders = signal<OrderSummary[]>([]);
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  switchScope(scope: Scope): void {
    if (scope !== this.scope()) {
      this.scope.set(scope);
      this.load(0);
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.load(this.currentPage() - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.load(this.currentPage() + 1);
    }
  }

  customerOf(order: OrderSummary): string {
    // Only the admin endpoint returns customerEmail; customer lists leave it empty.
    return (order as Partial<AdminOrderSummary>).customerEmail ?? '';
  }

  // Admin only. Marking as paid is allowed from PENDING; cancelling from PENDING or PAID.
  changeStatus(order: OrderSummary, status: 'PAID' | 'CANCELLED'): void {
    if (status === 'CANCELLED'
      && !window.confirm(`Cancel order ${order.orderNumber}? Its stock will be returned.`)) {
      return;
    }

    this.actionError.set(null);
    this.orderService.changeStatus(order.id, status).subscribe({
      next: () => this.load(this.currentPage()),
      error: (err: HttpErrorResponse) => {
        this.actionError.set(err.status === 409
          ? `Order ${order.orderNumber} cannot be changed to ${status} from its current status.`
          : 'Could not change the order status. Please try again.');
      },
    });
  }

  private load(page: number): void {
    this.loading.set(true);
    this.error.set(null);

    const request = this.scope() === 'all'
      ? this.orderService.getAllOrders(page)
      : this.orderService.getOrders(page);

    request.subscribe({
      next: (result) => {
        this.orders.set(result.content);
        this.currentPage.set(result.number);
        this.totalPages.set(result.totalPages);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.scope() === 'all'
          ? 'Could not load the orders. Please try again.'
          : 'Could not load your orders. Please try again.');
        this.loading.set(false);
      },
    });
  }
}
