import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';
import { Order } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-order-detail',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './order-detail.html',
  styleUrl: '../orders.css',
})
export class OrderDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);
  private readonly paymentService = inject(PaymentService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly order = signal<Order | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly paying = signal(false);
  protected readonly payError = signal<string | null>(null);

  // Stripe sends the user back to /orders/:id?payment=cancelled when they leave the payment page.
  protected readonly paymentCancelled = toSignal(
    this.route.queryParamMap.pipe(map((params) => params.get('payment') === 'cancelled')),
    { initialValue: false },
  );

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      tap(() => {
        this.loading.set(true);
        this.error.set(null);
        this.order.set(null);
      }),
      switchMap((id) => this.orderService.getOrder(id).pipe(
        catchError((err: HttpErrorResponse) => {
          this.error.set(err.status === 404
            ? 'This order does not exist.'
            : 'Could not load this order. Please try again.');
          this.loading.set(false);
          return EMPTY;
        }),
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((order) => {
      this.order.set(order);
      this.loading.set(false);
    });
  }

  // Starts a Stripe session for an existing PENDING order (e.g. after a cancelled payment).
  protected pay(order: Order): void {
    this.paying.set(true);
    this.payError.set(null);

    this.paymentService.createCheckoutSession(order.id).subscribe({
      next: (session) => {
        window.location.href = session.url;
      },
      error: (err: HttpErrorResponse) => {
        this.payError.set(err.status === 409
          ? 'This order has expired and its items were released. '
            + 'Please add the products to your cart and check out again.'
          : 'Could not start the payment. Please try again.');
        this.paying.set(false);
      },
    });
  }
}
