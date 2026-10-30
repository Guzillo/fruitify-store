import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  protected readonly auth = inject(AuthService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    if (this.auth.getAccessToken()) {
      this.reload();
    }
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);

    this.auth.loadCurrentUser().subscribe({
      next: () => this.loading.set(false),
      error: () => {
        this.error.set('Could not load your profile. Please try again.');
        this.loading.set(false);
      },
    });
  }
}
