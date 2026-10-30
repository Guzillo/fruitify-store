import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { AddToCart } from './add-to-cart';

describe('AddToCart', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddToCart],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should add the product to the cart when logged in', () => {
    localStorage.setItem('accessToken', 'test-token');
    const fixture = TestBed.createComponent(AddToCart);
    fixture.componentRef.setInput('productId', 2);
    fixture.componentRef.setInput('inStock', true);
    fixture.detectChanges();

    fixture.componentInstance['add']();

    const request = httpMock.expectOne('http://localhost:8080/api/cart/items');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ productId: 2, quantity: 1 });
    request.flush({ id: 10, productId: 2, productName: 'Apples', price: 2.49, quantity: 1 });

    expect(fixture.componentInstance['message']()).toBe('Added to cart.');
  });

  it('should send a logged-out visitor to login and remember the page', () => {
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'url', 'get').mockReturnValue('/products/2');

    const fixture = TestBed.createComponent(AddToCart);
    fixture.componentRef.setInput('productId', 2);
    fixture.componentRef.setInput('inStock', true);
    fixture.detectChanges();

    fixture.componentInstance['add']();

    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/products/2' } });
    httpMock.expectNone('http://localhost:8080/api/cart/items');
  });
});
