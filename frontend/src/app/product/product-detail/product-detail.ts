import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, switchMap, tap } from 'rxjs';
import { AddToCart } from '../../cart/add-to-cart/add-to-cart';
import { Product } from '../../models/product.model';
import { AuthService } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { ProductForm } from '../product-form/product-form';
import { LoadingState } from '../../shared/loading-state/loading-state';

@Component({
  selector: 'app-product-detail',
  imports: [CurrencyPipe, RouterLink, AddToCart, ProductForm, LoadingState],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css',
})
export class ProductDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly auth = inject(AuthService);

  protected readonly product = signal<Product | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly editing = signal(false);
  protected readonly adminBusy = signal(false);
  protected readonly adminError = signal<string | null>(null);

  ngOnInit(): void {
    // paramMap emits again when the URL changes, e.g. /products/1 -> /products/2.
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      tap(() => {
        this.loading.set(true);
        this.error.set(null);
        this.product.set(null);
        this.editing.set(false);
      }),
      switchMap((id) => this.productService.getProduct(id).pipe(
        catchError((err: HttpErrorResponse) => {
          this.error.set(err.status === 404
            ? 'This product does not exist.'
            : 'Could not load this product. Please try again.');
          this.loading.set(false);
          return EMPTY;
        }),
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((product) => {
      this.product.set(product);
      this.loading.set(false);
    });
  }

  protected onSaved(product: Product): void {
    this.product.set(product);
    this.editing.set(false);
  }

  // Hiding a product keeps it and its order history; deleting is only possible without orders.
  protected toggleVisibility(product: Product): void {
    this.adminBusy.set(true);
    this.adminError.set(null);

    this.productService.updateProduct(product.id, { isActive: !product.isActive }).subscribe({
      next: (updated) => {
        this.product.set(updated);
        this.adminBusy.set(false);
      },
      error: () => {
        this.adminError.set('Could not change the visibility of this product.');
        this.adminBusy.set(false);
      },
    });
  }

  protected delete(product: Product): void {
    if (!window.confirm(`Delete "${product.name}"? This cannot be undone.`)) {
      return;
    }

    this.adminBusy.set(true);
    this.adminError.set(null);

    this.productService.deleteProduct(product.id).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: (err: HttpErrorResponse) => {
        this.adminError.set(err.status === 409
          ? 'This product is part of existing orders, so it cannot be deleted. Hide it instead.'
          : 'Could not delete this product. Please try again.');
        this.adminBusy.set(false);
      },
    });
  }
}
