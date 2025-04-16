package ru.practicum.event.service.interfaces;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getAllEvents(List<Long> users, List<String> states, List<Long> categories,
                                    String rangeStart, String rangeEnd, int from, int size);

    EventFullDto editAndApproveEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}