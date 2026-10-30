import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
readonly auth = inject(AuthService);
  private readonly router = inject(Router);

readonly loading = signal(false);
readonly error = signal<string | null>(null);

readonly profileForm = new FormGroup({
    firstName: new FormControl('', { nonNullable: true }),
    lastName: new FormControl('', { nonNullable: true }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });
readonly savingProfile = signal(false);
readonly profileMessage = signal<string | null>(null);
readonly profileError = signal<string | null>(null);

readonly passwordForm = new FormGroup({
    currentPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8)],
    }),
  });
readonly savingPassword = signal(false);
readonly passwordMessage = signal<string | null>(null);
readonly passwordError = signal<string | null>(null);

  ngOnInit(): void {
    if (this.auth.getAccessToken()) {
      this.reload();
    }
  }

reload(): void {
    this.loading.set(true);
    this.error.set(null);

    this.auth.loadCurrentUser().subscribe({
      next: (user) => {
        this.profileForm.setValue({
          firstName: user.firstName ?? '',
          lastName: user.lastName ?? '',
          email: user.email,
        });
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your profile. Please try again.');
        this.loading.set(false);
      },
    });
  }

saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const { firstName, lastName, email } = this.profileForm.getRawValue();
    const emailChanged = email !== this.auth.currentUser()?.email;

    this.savingProfile.set(true);
    this.profileMessage.set(null);
    this.profileError.set(null);

    this.auth.updateProfile({
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: email.trim(),
    }).subscribe({
      next: () => {
        this.savingProfile.set(false);
        if (emailChanged) {
          // The access token still carries the old email, so the session must be restarted.
          this.auth.clear();
          this.router.navigateByUrl('/login');
          return;
        }
        this.profileMessage.set('Profile saved.');
      },
      error: () => {
        this.profileError.set('Could not save your profile. The email may already be in use.');
        this.savingProfile.set(false);
      },
    });
  }

changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.savingPassword.set(true);
    this.passwordMessage.set(null);
    this.passwordError.set(null);

    this.auth.changePassword(this.passwordForm.getRawValue()).subscribe({
      next: () => {
        this.savingPassword.set(false);
        this.passwordForm.reset();
        this.passwordMessage.set('Password changed.');
      },
      error: () => {
        // The backend answers a wrong current password with a 500 for now, so any error reads as this.
        this.passwordError.set('Could not change password. Check your current password.');
        this.savingPassword.set(false);
      },
    });
  }
}
