package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.request.model.enums.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.util.Constant.DATE_TIME_FORMAT;

@Data
public class ParticipationRequestDto {
    private Long id;

    private Long requester;

    private Long event;

    private RequestStatus status;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime created;
}