import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Category, Product } from '../models/product.model';
import { AddToCart } from '../cart/add-to-cart/add-to-cart';
import { CategoryService } from '../services/category.service';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-home',
  imports: [CurrencyPipe, RouterLink, AddToCart],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);

  protected readonly products = signal<Product[]>([]);
  protected readonly categories = signal<Category[]>([]);
  protected readonly selectedCategoryId = signal<number | null>(null);
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadCategories();
    this.loadPage(0);
  }

  protected selectCategory(categoryId: number | null): void {
    if (categoryId === this.selectedCategoryId()) {
      return;
    }
    this.selectedCategoryId.set(categoryId);
    this.loadPage(0);
  }

  protected loadPage(page: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.productService.getProducts(page, 12, this.selectedCategoryId() ?? undefined).subscribe({
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

  private loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.error.set('Could not load categories.'),
    });
  }
}
