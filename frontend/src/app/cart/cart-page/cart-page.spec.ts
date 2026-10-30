import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { CartPage } from './cart-page';

describe('CartPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load the cart and compute the total', () => {
    const fixture = TestBed.createComponent(CartPage);
    fixture.detectChanges();

    httpMock.expectOne('http://localhost:8080/api/cart').flush({
      id: 1,
      items: [
        { id: 10, productId: 2, productName: 'Apples', price: 2.49, quantity: 2 },
      ],
    });

    expect(fixture.componentInstance.cart()?.items.length).toBe(1);
    expect(fixture.componentInstance.total()).toBe(4.98);
  });

  it('should send the new quantity and reload the cart', () => {
    const fixture = TestBed.createComponent(CartPage);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/cart').flush({
      id: 1,
      items: [{ id: 10, productId: 2, productName: 'Apples', price: 2.49, quantity: 2 }],
    });

    fixture.componentInstance.changeQuantity(
      fixture.componentInstance.cart()!.items[0], 3);

    const update = httpMock.expectOne({
      method: 'PUT',
      url: 'http://localhost:8080/api/cart/items/10',
    });
    expect(update.request.body).toEqual({ quantity: 3 });
    update.flush({ id: 10, productId: 2, productName: 'Apples', price: 2.49, quantity: 3 });

    httpMock.expectOne('http://localhost:8080/api/cart').flush({
      id: 1,
      items: [{ id: 10, productId: 2, productName: 'Apples', price: 2.49, quantity: 3 }],
    });
    expect(fixture.componentInstance.total()).toBe(7.47);
  });
});
