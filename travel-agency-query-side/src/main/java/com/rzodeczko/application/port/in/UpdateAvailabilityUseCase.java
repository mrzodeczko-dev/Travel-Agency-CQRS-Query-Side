package com.rzodeczko.application.port.in;


import com.rzodeczko.application.command.UpdateAvailabilityCommand;

public interface UpdateAvailabilityUseCase {
    void update(UpdateAvailabilityCommand command);
}
