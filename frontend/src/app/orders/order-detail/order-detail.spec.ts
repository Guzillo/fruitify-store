import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Order } from '../../models/order.model';
import { OrderDetail } from './order-detail';

const pendingOrder: Order = {
  id: 5,
  orderNumber: 'ORD-ABC123',
  status: 'PENDING',
  totalAmount: 4.98,
  createdAt: '2026-10-30T09:00:00Z',
  shippingAddress: {
    recipientName: 'Jan Kowalski',
    street: 'Marszałkowska 1',
    city: 'Warsaw',
    postalCode: '00-001',
    country: 'PL',
  },
  items: [{ productId: 2, productName: 'Apples', priceAtPurchase: 2.49, quantity: 2 }],
};

describe('OrderDetail', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderDetail],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: '5' })),
            queryParamMap: of(convertToParamMap({})),
          },
        },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('should cancel a pending order after confirmation and show the cancelled status', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(OrderDetail);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/orders/5').flush(pendingOrder);

    fixture.componentInstance['cancelOrder'](pendingOrder);

    const request = httpMock.expectOne({
      method: 'POST',
      url: 'http://localhost:8080/api/orders/5/cancel',
    });
    request.flush({ ...pendingOrder, status: 'CANCELLED' });

    expect(fixture.componentInstance['order']()?.status).toBe('CANCELLED');
  });

  it('should not call the server when the user declines the confirmation', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    const fixture = TestBed.createComponent(OrderDetail);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/orders/5').flush(pendingOrder);

    fixture.componentInstance['cancelOrder'](pendingOrder);

    httpMock.expectNone('http://localhost:8080/api/orders/5/cancel');
  });
});
