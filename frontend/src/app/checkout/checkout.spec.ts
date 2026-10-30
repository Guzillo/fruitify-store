import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Checkout } from './checkout';

function setup(queryParams: Record<string, string> = {}) {
  TestBed.configureTestingModule({
    imports: [Checkout],
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: ActivatedRoute, useValue: { queryParamMap: of(convertToParamMap(queryParams)) } },
    ],
  });
  const fixture = TestBed.createComponent(Checkout);
  const httpMock = TestBed.inject(HttpTestingController);
  return { fixture, httpMock };
}

describe('Checkout', () => {
  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('should show the thank-you message when Stripe returns with success=true', () => {
    const { fixture } = setup({ success: 'true' });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Thank you!');
  });

  it('should create the order, then start the Stripe session, then redirect', () => {
    const { fixture, httpMock } = setup();
    fixture.detectChanges();
    const redirect = vi
      .spyOn(fixture.componentInstance as unknown as { redirectTo(url: string): void }, 'redirectTo')
      .mockImplementation(() => undefined);

    fixture.componentInstance.form.setValue({
      recipientName: 'Jan Kowalski',
      street: 'Marszałkowska 1',
      city: 'Warsaw',
      postalCode: '00-001',
      country: 'PL',
    });
    fixture.componentInstance['placeOrder']();

    const createOrder = httpMock.expectOne('http://localhost:8080/api/orders');
    expect(createOrder.request.body).toEqual({
      shippingAddress: {
        recipientName: 'Jan Kowalski',
        street: 'Marszałkowska 1',
        city: 'Warsaw',
        postalCode: '00-001',
        country: 'PL',
      },
    });
    createOrder.flush({ id: 5, orderNumber: 'ORD-ABC123', status: 'PENDING' });

    const checkout = httpMock.expectOne('http://localhost:8080/api/payments/create-checkout-session');
    expect(checkout.request.body).toEqual({ orderNumber: '5' });
    checkout.flush({ url: 'https://checkout.stripe.test/session', sessionId: 'cs_test' });

    expect(redirect).toHaveBeenCalledWith('https://checkout.stripe.test/session');
  });
});
