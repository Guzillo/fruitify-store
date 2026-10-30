import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap } from 'rxjs';
import { OrderService } from '../services/order.service';
import { PaymentService } from '../services/payment.service';

@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './checkout.html',
  styleUrl: '../auth/auth-form.css',
})
export class Checkout {
  private readonly orders = inject(OrderService);
  private readonly payments = inject(PaymentService);
  private readonly route = inject(ActivatedRoute);

  // Stripe sends the user back to /checkout?success=true (see stripe.domain + PaymentService).
  protected readonly returnedFromPayment = toSignal(
    this.route.queryParamMap.pipe(map((params) => params.get('success') === 'true')),
    { initialValue: false },
  );

  readonly form = new FormGroup({
    recipientName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    street: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    city: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    postalCode: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(32)],
    }),
    country: new FormControl('PL', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
  });

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected placeOrder(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    this.orders.createOrder(this.form.getRawValue()).pipe(
      switchMap((order) => this.payments.createCheckoutSession(order.id).pipe(
        catchError(() => {
          // The order exists now, so the user can pay for it later from "My orders".
          this.error.set('Your order was created, but the payment could not start. '
            + 'You can pay for it from My orders.');
          this.submitting.set(false);
          return EMPTY;
        }),
      )),
    ).subscribe({
      next: (session) => this.redirectTo(session.url),
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 409
          ? 'Your cart is empty or some items are no longer available.'
          : 'Could not place the order. Please try again.');
        this.submitting.set(false);
      },
    });
  }

  private redirectTo(url: string): void {
    window.location.href = url;
  }
}
