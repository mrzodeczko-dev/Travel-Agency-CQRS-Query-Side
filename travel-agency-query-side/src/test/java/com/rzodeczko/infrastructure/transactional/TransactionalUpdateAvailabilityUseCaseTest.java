package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.command.UpdateAvailabilityCommand;
import com.rzodeczko.application.port.in.UpdateAvailabilityUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionalUpdateAvailabilityUseCaseTest {

    @Mock
    private UpdateAvailabilityUseCase delegate;

    private TransactionalUpdateAvailabilityUseCase useCase;

    private static final long HOTEL_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2024, 6, 1);
    private static final long OCCUPIED = 50L;

    @BeforeEach
    void setUp() {
        useCase = new TransactionalUpdateAvailabilityUseCase(delegate);
    }

    @Test
    void update_delegatesToInner() {
        UpdateAvailabilityCommand command = new UpdateAvailabilityCommand(HOTEL_ID, DATE, OCCUPIED);

        useCase.update(command);

        verify(delegate, only()).update(command);
    }
}
