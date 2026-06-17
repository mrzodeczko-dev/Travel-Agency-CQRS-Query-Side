package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.command.UpdateAvailabilityCommand;
import com.rzodeczko.application.port.in.UpdateAvailabilityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@RequiredArgsConstructor
public class RetryingUpdateAvailabilityUseCase implements UpdateAvailabilityUseCase {

    private final UpdateAvailabilityUseCase delegate;

    @Override
    @Retryable(
            retryFor = DuplicateKeyException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50))
    public void update(UpdateAvailabilityCommand command) {
        delegate.update(command);
    }
}
