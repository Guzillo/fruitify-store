import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Product, ProductPage, ProductWriteRequest } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/products';

  getProducts(page = 0, size = 12, categoryId?: number): Observable<ProductPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'name,asc');

    if (categoryId !== undefined) {
      params = params.set('categoryId', categoryId);
    }

    return this.http.get<ProductPage>(this.apiUrl, { params });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  // Admin only; the server rejects other users with 403.
  createProduct(request: ProductWriteRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, request);
  }

  updateProduct(id: number, request: Partial<ProductWriteRequest>): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
