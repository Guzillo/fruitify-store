import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Cart, CartItem } from '../../models/cart.model';
import { CartService } from '../../services/cart.service';
import { LoadingState } from '../../shared/loading-state/loading-state';

@Component({
  selector: 'app-cart-page',
  imports: [CurrencyPipe, RouterLink, LoadingState],
  templateUrl: './cart-page.html',
  styleUrl: './cart-page.css',
})
export class CartPage implements OnInit {
  private readonly cartService = inject(CartService);

  readonly cart = signal<Cart | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly total = computed(() => {
    const items = this.cart()?.items ?? [];
    const sum = items.reduce((acc, item) => acc + item.price * item.quantity, 0);
    return Math.round(sum * 100) / 100;
  });

  ngOnInit(): void {
    this.loading.set(true);
    this.load();
  }

  changeQuantity(item: CartItem, quantity: number): void {
    this.cartService.updateQuantity(item.id, quantity).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not update the quantity.'),
    });
  }

  remove(item: CartItem): void {
    this.cartService.removeItem(item.id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not remove the item.'),
    });
  }

  clear(): void {
    this.cartService.clear().subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not clear the cart.'),
    });
  }

  private load(): void {
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart.set(cart);
        this.error.set(null);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not load your cart.'),
    });
  }

  private showError(err: HttpErrorResponse, fallback: string): void {
    // 409 comes from stock checks when raising a quantity.
    this.error.set(err.status === 409
      ? 'Not enough stock for that quantity.'
      : fallback);
    this.loading.set(false);
  }
}
