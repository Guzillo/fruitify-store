import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../auth-form.css',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // Same rules as the backend's SignupUserDto (email required, password at least 8 chars).
  protected readonly form = new FormGroup({
    firstName: new FormControl('', { nonNullable: true }),
    lastName: new FormControl('', { nonNullable: true }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8)],
    }),
  });

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { firstName, lastName, email, password } = this.form.getRawValue();
    this.submitting.set(true);
    this.error.set(null);

    this.auth.signup({
      email,
      password,
      firstName: firstName.trim() || null,
      lastName: lastName.trim() || null,
    }).pipe(
      // Signup creates the account only; sign in right away so the user lands logged in.
      switchMap(() => this.auth.signin(email, password)),
      switchMap(() => this.auth.loadCurrentUser()),
    ).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 409
          ? 'An account with this email already exists.'
          : 'Registration failed. Please try again.');
        this.submitting.set(false);
      },
    });
  }
}
