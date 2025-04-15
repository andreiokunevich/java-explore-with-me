package ru.practicum.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.request.model.enums.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    @NotNull
    private List<Long> requestIds;

    @NotNull
    private RequestStatus status;
}