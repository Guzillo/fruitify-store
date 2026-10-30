import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ProductForm } from './product-form';

describe('ProductForm', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductForm],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create a product with POST /api/products', () => {
    const fixture = TestBed.createComponent(ProductForm);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/categories').flush([
      { id: 1, name: 'Fruit', slug: 'fruit' },
    ]);

    fixture.componentInstance.form.setValue({
      name: '  Pears ',
      description: 'Sweet pears',
      price: 3.5,
      stockQuantity: 12,
      imageUrl: '',
      categoryId: 1,
      isActive: true,
    });
    fixture.componentInstance.submit();

    const request = httpMock.expectOne({ method: 'POST', url: 'http://localhost:8080/api/products' });
    expect(request.request.body).toEqual({
      name: 'Pears',
      description: 'Sweet pears',
      price: 3.5,
      stockQuantity: 12,
      isActive: true,
      imageUrl: null,
      categoryId: 1,
    });
    request.flush({ id: 9, name: 'Pears' });
  });

  it('should update an existing product with PUT /api/products/{id}', () => {
    const fixture = TestBed.createComponent(ProductForm);
    fixture.componentRef.setInput('product', {
      id: 4,
      name: 'Apples',
      description: 'Crisp',
      price: 2.49,
      stockQuantity: 25,
      imageUrl: null,
      isActive: true,
      category: { id: 1, name: 'Fruit', slug: 'fruit' },
    });
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/categories').flush([]);

    fixture.componentInstance.form.controls.price.setValue(2.99);
    fixture.componentInstance.submit();

    const request = httpMock.expectOne({ method: 'PUT', url: 'http://localhost:8080/api/products/4' });
    expect(request.request.body).toMatchObject({ name: 'Apples', price: 2.99, categoryId: 1 });
    request.flush({ id: 4 });
  });
});
