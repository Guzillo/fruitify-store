import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-add-to-cart',
  templateUrl: './add-to-cart.html',
  styleUrl: './add-to-cart.css',
})
export class AddToCart {
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);

  readonly productId = input.required<number>();
  readonly inStock = input.required<boolean>();

  protected readonly adding = signal(false);
  protected readonly message = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected add(): void {
    // Checked against the stored token, so it works right after a page reload too.
    if (!this.auth.getAccessToken()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    this.adding.set(true);
    this.message.set(null);
    this.error.set(null);

    this.cart.addItem(this.productId()).subscribe({
      next: () => {
        this.adding.set(false);
        this.message.set('Added to cart.');
      },
      error: (err: HttpErrorResponse) => {
        this.adding.set(false);
        // 409 means not enough stock, or the product is no longer sold.
        this.error.set(err.status === 409
          ? 'Not available right now (out of stock or no longer sold).'
          : 'Could not add to cart. Please try again.');
      },
    });
  }
}
