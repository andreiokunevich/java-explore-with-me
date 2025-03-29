package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.EndpointHitDto;
import ru.practicum.model.EndpointHit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointHitMapper {
    public static EndpointHitDto endpointHitToDto(EndpointHit endpointHit) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setId(endpointHit.getId());
        endpointHitDto.setApp(endpointHit.getApp());
        endpointHitDto.setIp(endpointHit.getIp());
        endpointHitDto.setUri(endpointHit.getUri());
        endpointHitDto.setTimestamp(endpointHit.getTimestamp());
        return endpointHitDto;
    }

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setTimestamp(endpointHitDto.getTimestamp());
        return endpointHit;
    }
}