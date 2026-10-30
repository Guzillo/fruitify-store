import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Profile } from './profile';

describe('Profile', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('accessToken', 'test-token');
    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should load the current user into the profile form', () => {
    const fixture = TestBed.createComponent(Profile);
    fixture.detectChanges();

    httpMock.expectOne('http://localhost:8080/api/users/me')
      .flush({ email: 'jan@example.com', firstName: 'Jan', lastName: 'Kowalski' });

    expect(fixture.componentInstance.profileForm.getRawValue()).toEqual({
      firstName: 'Jan',
      lastName: 'Kowalski',
      email: 'jan@example.com',
    });
  });

  it('should send changed profile fields with PUT /users/me', () => {
    const fixture = TestBed.createComponent(Profile);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/users/me')
      .flush({ email: 'jan@example.com', firstName: 'Jan', lastName: 'Kowalski' });

    fixture.componentInstance.profileForm.controls.firstName.setValue('Janek');
    fixture.componentInstance.saveProfile();

    const request = httpMock.expectOne({ method: 'PUT', url: 'http://localhost:8080/api/users/me' });
    expect(request.request.body).toEqual({
      firstName: 'Janek',
      lastName: 'Kowalski',
      email: 'jan@example.com',
    });
    request.flush({ email: 'jan@example.com', firstName: 'Janek', lastName: 'Kowalski' });

    expect(fixture.componentInstance.profileMessage()).toBe('Profile saved.');
  });

  it('should send a password change with PUT /users/me/password', () => {
    const fixture = TestBed.createComponent(Profile);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/users/me')
      .flush({ email: 'jan@example.com', firstName: null, lastName: null });

    fixture.componentInstance.passwordForm.setValue({
      currentPassword: 'old-password',
      newPassword: 'new-password-123',
    });
    fixture.componentInstance.changePassword();

    const request = httpMock.expectOne({
      method: 'PUT',
      url: 'http://localhost:8080/api/users/me/password',
    });
    expect(request.request.body).toEqual({
      currentPassword: 'old-password',
      newPassword: 'new-password-123',
    });
    request.flush(null);

    expect(fixture.componentInstance.passwordMessage()).toBe('Password changed.');
  });
});
