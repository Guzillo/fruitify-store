import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Category, Product, ProductWriteRequest } from '../../models/product.model';
import { CategoryService } from '../../services/category.service';
import { ProductService } from '../../services/product.service';

// Admin form used both to create a product (no input) and to edit one (pass `product`).
@Component({
  selector: 'app-product-form',
  imports: [ReactiveFormsModule],
  templateUrl: './product-form.html',
  styleUrl: './product-form.css',
})
export class ProductForm implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);

  readonly product = input<Product | null>(null);
  readonly saved = output<Product>();
  readonly cancelled = output<void>();

  readonly categories = signal<Category[]>([]);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(2048)],
    }),
    price: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)],
    }),
    stockQuantity: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)],
    }),
    imageUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(255)],
    }),
    categoryId: new FormControl<number | null>(null, Validators.required),
    isActive: new FormControl(true, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.categoryService.getCategories().subscribe((categories) => this.categories.set(categories));

    const existing = this.product();
    if (existing) {
      this.form.setValue({
        name: existing.name,
        description: existing.description,
        price: existing.price,
        stockQuantity: existing.stockQuantity,
        imageUrl: existing.imageUrl ?? '',
        categoryId: existing.category.id,
        isActive: existing.isActive,
      });
    }
  }

  submit(): void {
    const raw = this.form.getRawValue();
    if (this.form.invalid || raw.categoryId === null) {
      this.form.markAllAsTouched();
      return;
    }

    const body: ProductWriteRequest = {
      name: raw.name.trim(),
      description: raw.description.trim(),
      price: Number(raw.price),
      stockQuantity: Number(raw.stockQuantity),
      isActive: raw.isActive,
      imageUrl: raw.imageUrl.trim() || null,
      categoryId: raw.categoryId,
    };

    this.submitting.set(true);
    this.error.set(null);

    const existing = this.product();
    const request = existing
      ? this.productService.updateProduct(existing.id, body)
      : this.productService.createProduct(body);

    request.subscribe({
      next: (product) => {
        this.submitting.set(false);
        this.saved.emit(product);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 409
          ? 'A product with this name already exists.'
          : 'Could not save the product. Please check the values and try again.');
        this.submitting.set(false);
      },
    });
  }
}
