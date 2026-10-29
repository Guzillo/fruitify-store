import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProductPage } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/products';

  getProducts(page = 0, size = 12): Observable<ProductPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'name,asc');

    return this.http.get<ProductPage>(this.apiUrl, { params });
  }
}
