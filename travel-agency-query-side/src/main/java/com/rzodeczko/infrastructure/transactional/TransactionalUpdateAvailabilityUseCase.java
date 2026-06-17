package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.command.UpdateAvailabilityCommand;
import com.rzodeczko.application.port.in.UpdateAvailabilityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalUpdateAvailabilityUseCase implements UpdateAvailabilityUseCase {

    private final UpdateAvailabilityUseCase delegate;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void update(UpdateAvailabilityCommand command) {
        delegate.update(command);
    }
}
