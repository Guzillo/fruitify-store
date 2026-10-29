import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Product } from '../models/product.model';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-home',
  imports: [CurrencyPipe],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private readonly productService = inject(ProductService);

  protected readonly products = signal<Product[]>([]);
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadPage(0);
  }

  protected loadPage(page: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.productService.getProducts(page).subscribe({
      next: (result) => {
        this.products.set(result.content);
        this.currentPage.set(result.number);
        this.totalPages.set(result.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load products. Is the backend running on port 8080?');
        this.loading.set(false);
      },
    });
  }

  protected previousPage(): void {
    if (this.currentPage() > 0) {
      this.loadPage(this.currentPage() - 1);
    }
  }

  protected nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.loadPage(this.currentPage() + 1);
    }
  }
}
