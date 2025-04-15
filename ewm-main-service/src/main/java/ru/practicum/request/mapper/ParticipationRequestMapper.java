package ru.practicum.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipationRequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        ParticipationRequestDto prDto = new ParticipationRequestDto();
        prDto.setId(participationRequest.getId());
        prDto.setRequester(participationRequest.getRequester().getId());
        prDto.setEvent(participationRequest.getEvent().getId());
        prDto.setCreated(participationRequest.getCreated());
        prDto.setStatus(participationRequest.getStatus());
        return prDto;
    }
}