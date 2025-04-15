package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.enums.RequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.request.service.interfaces.ParticipationRequestService;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        List<ParticipationRequest> requestsList = participationRequestRepository.findByRequesterId(userId);

        if (requestsList.isEmpty()) {
            return new ArrayList<>();
        }

        return requestsList.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " не найдено."));

        if (event.getParticipantLimit().equals(event.getConfirmedRequests()) && event.getParticipantLimit() != 0) {
            throw new ConflictException("В данном мероприятии больше нет свободных мест.");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Данное мероприятие еще не имеет статуса PUBLISHED.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор мероприятия не может подавать заявку на участие в своем мероприятии.");
        }

        ParticipationRequest requestCheck = participationRequestRepository.findByRequesterIdAndEventId(userId, eventId);

        if (requestCheck != null) {
            throw new ConflictException("Запрос на участии в мероприятии от данного пользовтаеля уже существует.");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        if ((!event.getRequestModeration() && event.getParticipantLimit() > event.getConfirmedRequests()) || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else if (!event.getRequestModeration() && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            request.setStatus(RequestStatus.REJECTED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        eventRepository.save(event);

        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявик на участие с id = " + requestId + " не найдено."));

        request.setStatus(RequestStatus.CANCELED);

        Event eventRequest = request.getEvent();
        if (eventRequest.getRequestModeration()) {
            eventRequest.setConfirmedRequests(eventRequest.getConfirmedRequests() - 1);
        }

        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }
}