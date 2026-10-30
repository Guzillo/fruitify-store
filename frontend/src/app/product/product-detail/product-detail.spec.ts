import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ProductDetail } from './product-detail';

describe('ProductDetail', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetail],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '2' })) } },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load the product from the id in the route and render it', () => {
    const fixture = TestBed.createComponent(ProductDetail);
    fixture.detectChanges();

    httpMock.expectOne('http://localhost:8080/api/products/2').flush({
      id: 2,
      name: 'Apples',
      description: 'Crisp red apples',
      price: 2.49,
      stockQuantity: 25,
      imageUrl: null,
      category: { id: 1, name: 'Fruit', slug: 'fruit' },
    });
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Apples');
    expect(text).toContain('Crisp red apples');
    expect(text).toContain('In stock');
  });

  it('should show a message when the product does not exist', () => {
    const fixture = TestBed.createComponent(ProductDetail);
    fixture.detectChanges();

    httpMock.expectOne('http://localhost:8080/api/products/2')
      .flush(null, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('This product does not exist.');
  });
});
