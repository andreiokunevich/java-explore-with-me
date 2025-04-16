package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.enums.State;
import ru.practicum.event.model.enums.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.event.service.interfaces.PrivateEventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.enums.RequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    private static final Long MIN_HOURS_BEFORE_START_EVENT = 2L;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEvents(Long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findByInitiatorId(userId, pageable).getContent().stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        Long categoryId = newEventDto.getCategory();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категории с id = " + categoryId + " не существует."));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_START_EVENT))) {
            throw new ConflictException("Дата и время на которые намечено событие не может быть раньше," +
                    " чем через два часа от текущего момента");
        }

        Location savedLocation = locationRepository.save(newEventDto.getLocation());

        Event event = EventMapper.toEvent(newEventDto, category, user);
        event.setLocation(savedLocation);

        Event savedEvent = eventRepository.save(event);

        return EventMapper.toEventFullDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ValidationException("Пользователь не создавал это мероприятие.");
        }

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event eventFromRepo = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        if (!eventFromRepo.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не создавал это мероприятие.");
        }

        if (eventFromRepo.getState() == State.PUBLISHED) {
            throw new ConflictException("Событие уже опубликовано, его нельзя отредактировать.");
        }

        if (updateEventUserRequest.getEventDate() != null &&
                updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_START_EVENT))) {
            throw new ConflictException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        if (updateEventUserRequest.getCategory() != null) {
            Long catId = updateEventUserRequest.getCategory();
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException("Категории с id " + catId + " не существует."));
            eventFromRepo.setCategory(category);
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            eventFromRepo.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            eventFromRepo.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getDescription() != null) {
            eventFromRepo.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            eventFromRepo.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            Location savedLocation = locationRepository.save(updateEventUserRequest.getLocation());
            eventFromRepo.setLocation(savedLocation);
        }

        if (updateEventUserRequest.getPaid() != null) {
            eventFromRepo.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            eventFromRepo.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getTitle() != null) {
            eventFromRepo.setTitle(updateEventUserRequest.getTitle());
        }

        if (updateEventUserRequest.getStateAction() == StateAction.SEND_TO_REVIEW) {
            eventFromRepo.setState(State.PENDING);
        } else if (updateEventUserRequest.getStateAction() == StateAction.CANCEL_REVIEW) {
            eventFromRepo.setState(State.CANCELED);
        }

        return EventMapper.toEventFullDto(eventFromRepo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByEventId(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        List<ParticipationRequest> participationRequestList = participationRequestRepository.findByEventId(eventId);

        return participationRequestList.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult approveRequests(Long userId, Long eventId,
                                                          EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        List<ParticipationRequest> requests = participationRequestRepository
                .findRequestByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (requests.isEmpty()) {
            throw new NotFoundException("Заявки на участие не найдены.");
        }

        if (eventRequestStatusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
            Long confirmedCount = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            long availableSlots = event.getParticipantLimit() - confirmedCount;

            if (event.getParticipantLimit() != 0 && event.getRequestModeration()
                    && eventRequestStatusUpdateRequest.getRequestIds().size() > availableSlots) {
                throw new ConflictException("Превышен лимит участников. Все заявки не могут быть подтверждены.");
            }
        }

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь на является создателем этого события.");
        }

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("В списке заявок присутствует(ют) заявки, не относящиеся к данному мероприятию.");
            }
        }

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания.");
            }
        }

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();
        Long countConfirmedRequests = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        for (ParticipationRequest request : requests) {
            if (eventRequestStatusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() == 0 || countConfirmedRequests < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(request);
                    countConfirmedRequests++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(request);
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        participationRequestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmedRequestsDto = confirmedRequests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
        List<ParticipationRequestDto> rejectedRequestsDto = rejectedRequests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
        return new EventRequestStatusUpdateResult(confirmedRequestsDto, rejectedRequestsDto);
    }
}