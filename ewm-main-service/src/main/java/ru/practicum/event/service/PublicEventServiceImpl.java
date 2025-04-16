package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.Sort;
import ru.practicum.event.model.enums.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.interfaces.PublicEventService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.util.Constant.FORMATTER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    private static final Long PLUS_YEARS_IF_RANGE_END_NULL = 10L;
    private static final String APP_NAME = "ewm-main-service";

    @Override
    public List<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                            String rangeEnd, boolean onlyAvailable, Sort sort, int from, int size,
                                            HttpServletRequest request) {

        LocalDateTime start = (rangeStart != null)
                ? LocalDateTime.parse(rangeStart, FORMATTER)
                : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null)
                ? LocalDateTime.parse(rangeEnd, FORMATTER)
                : LocalDateTime.now().plusYears(PLUS_YEARS_IF_RANGE_END_NULL);

        Pageable pageable = PageRequest.of(from / size, size);

        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Ошибка при задании временного промежутка.");
        }

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.createHit(endpointHitDto);

        List<Event> events = eventRepository.searchEvents(
                        text,
                        categories,
                        paid,
                        start,
                        end,
                        onlyAvailable,
                        pageable)
                .getContent();

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        if (events.stream().noneMatch(event -> event.getState() == State.PUBLISHED)) {
            throw new ValidationException("Опубликованные события отсутствуют.");
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        ResponseEntity<List<ViewStatsDto>> response = statsClient.getStats(rangeStart, rangeEnd, uris, true);
        List<ViewStatsDto> stats = response.getBody();

        List<EventShortDto> result;
        if (stats != null) {
            Map<String, Long> viewsMap = stats.stream()
                    .collect(Collectors.toMap(
                            ViewStatsDto::getUri,
                            ViewStatsDto::getHits
                    ));

            result = new ArrayList<>(events.stream()
                    .map(event -> {
                        EventShortDto dto = EventMapper.toEventShortDto(event);
                        String eventUri = "/events/" + event.getId();
                        dto.setViews(Math.toIntExact(viewsMap.getOrDefault(eventUri, 0L)));
                        return dto;
                    })
                    .toList());
        } else {
            result = new ArrayList<>(events.stream()
                    .map(event -> {
                        EventShortDto dto = EventMapper.toEventShortDto(event);
                        dto.setViews(0);
                        return dto;
                    })
                    .toList());
        }

        if (sort != null) {
            if (sort == Sort.EVENT_DATE) {
                result.sort(Comparator.comparing(EventShortDto::getEventDate));
            } else if (sort == Sort.VIEWS) {
                result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            }
        }

        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Такого события не существует"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Такое событие со статусом PUBLISHED не найдено");
        }

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.createHit(endpointHitDto);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);

        String start = LocalDateTime.now().minusYears(50).format(FORMATTER);
        String end = LocalDateTime.now().plusYears(50).format(FORMATTER);

        ResponseEntity<List<ViewStatsDto>> response = statsClient.getStats(start,
                end,
                List.of(request.getRequestURI()),
                true);

        List<ViewStatsDto> stats = response.getBody();

        Long views = 0L;
        if (stats != null && !stats.isEmpty()) {
            views = stats.getFirst().getHits();
        }

        eventFullDto.setViews(Math.toIntExact(views));

        return eventFullDto;
    }
}