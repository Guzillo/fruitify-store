import { Component, OnDestroy, OnInit, computed, input, signal } from '@angular/core';

/**
 * Playful loading indicator with an elapsed-time counter.
 * Used while waiting for API responses (the free Render backend can take up to ~2 minutes to wake).
 */
@Component({
  selector: 'app-loading-state',
  templateUrl: './loading-state.html',
  styleUrl: './loading-state.css',
})
export class LoadingState implements OnInit, OnDestroy {
  readonly message = input('Loading…');
  readonly hint = input('');
  readonly compact = input(false);

  protected readonly elapsedSeconds = signal(0);

  protected readonly elapsedLabel = computed(() => {
    const total = this.elapsedSeconds();
    const minutes = Math.floor(total / 60);
    const seconds = total % 60;
    return minutes > 0
      ? `${minutes}m ${seconds.toString().padStart(2, '0')}s`
      : `${seconds}s`;
  });

  private timerId: ReturnType<typeof setInterval> | undefined;

  ngOnInit(): void {
    this.timerId = setInterval(() => this.elapsedSeconds.update((value) => value + 1), 1000);
  }

  ngOnDestroy(): void {
    if (this.timerId !== undefined) {
      clearInterval(this.timerId);
    }
  }
}
