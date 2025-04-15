package ru.practicum.event.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.enums.Sort;

import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid,
                                     String rangeStart, String rangeEnd, boolean onlyAvailable,
                                     Sort sort, int from, int size,
                                     HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);
}