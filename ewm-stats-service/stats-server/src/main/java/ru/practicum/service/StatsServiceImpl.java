package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;
import ru.practicum.service.interfaces.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;

    @Override
    public EndpointHitDto createHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto);
        return EndpointHitMapper.endpointHitToDto(repository.save(endpointHit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (start == null || end == null) {
            throw new ValidationException("Даты не переданы.");
        }

        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Даты противоречат друг другу!");
        }

        if (unique) {
            return repository.findAllWithUniqueIpTrue(start, end, uris);
        } else {
            return repository.findAllWithUniqueIpFalse(start, end, uris);
        }
    }
}