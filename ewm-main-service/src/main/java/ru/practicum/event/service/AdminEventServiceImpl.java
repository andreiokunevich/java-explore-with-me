package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.enums.State;
import ru.practicum.event.model.enums.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.event.service.interfaces.AdminEventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.model.enums.RequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.util.Constant.DATE_TIME_FORMAT;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private static final Sort SORT_BY_ID_ASC = Sort.by("id").ascending();
    private static final Long MAX_HOURS_BEFORE_START_EVENT = 1L;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEvents(List<Long> users, List<String> states, List<Long> categories,
                                           String rangeStart, String rangeEnd, int from, int size) {

        LocalDateTime start = (rangeStart != null)
                ? LocalDateTime.parse(rangeStart, FORMATTER)
                : LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0);

        LocalDateTime end = (rangeEnd != null)
                ? LocalDateTime.parse(rangeEnd, FORMATTER)
                : LocalDateTime.of(2100, 1, 1, 1, 1, 1, 0);

        if (!start.isBefore(end)) {
            throw new ValidationException("Ошибка при задании временного промежутка: дата начала должна быть раньше даты окончания.");
        }

        Pageable pageable = PageRequest.of(from / size, size, SORT_BY_ID_ASC);

        List<Long> userIds = (users == null || users.isEmpty())
                ? userRepository.findAll().stream()
                .map(User::getId)
                .toList()
                : users;

        List<Long> categoryIds = (categories == null || categories.isEmpty())
                ? categoryRepository.findAll().stream()
                .map(Category::getId)
                .toList()
                : categories;

        List<State> stateList = (states == null || states.isEmpty())
                ? List.of(State.PENDING, State.PUBLISHED, State.CANCELED)
                : states.stream()
                .map(State::valueOf)
                .toList();

        List<Event> events = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
                userIds, stateList, categoryIds, start, end, pageable).getContent();

        Map<Long, Long> confirmedRequestsMap = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> participationRequestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED)
                ));

        List<EventFullDto> eventFullDtos = events.stream()
                .map(EventMapper::toEventFullDto)
                .toList();

        eventFullDtos.forEach(dto -> dto.setConfirmedRequests(
                Math.toIntExact(confirmedRequestsMap.getOrDefault(dto.getId(), 0L))
        ));

        return eventFullDtos;
    }

    @Override
    public EventFullDto editAndApproveEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event eventFromRepo = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));

        if (updateEventAdminRequest.getEventDate() != null &&
                updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(MAX_HOURS_BEFORE_START_EVENT))) {
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем через " + MAX_HOURS_BEFORE_START_EVENT + " часов.");
        }

        if (eventFromRepo.getState() == State.PUBLISHED || eventFromRepo.getState() == State.CANCELED) {
            throw new ConflictException("Редактирование статуса недоступно для опубликованного или отмененного события.");
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            eventFromRepo.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            eventFromRepo.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getDescription() != null) {
            eventFromRepo.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            eventFromRepo.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            Location savedLocation = locationRepository.save(updateEventAdminRequest.getLocation());
            eventFromRepo.setLocation(savedLocation);
        }

        if (updateEventAdminRequest.getPaid() != null) {
            eventFromRepo.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            eventFromRepo.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getTitle() != null) {
            eventFromRepo.setTitle(updateEventAdminRequest.getTitle());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Long catId = updateEventAdminRequest.getCategory();
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException("Категории с id " + catId + " не существует."));
            eventFromRepo.setCategory(category);
        }

        if (eventFromRepo.getState() == State.PENDING && updateEventAdminRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
            eventFromRepo.setState(State.PUBLISHED);
            eventFromRepo.setPublishedOn(LocalDateTime.now());
        }
        if (eventFromRepo.getState() == State.PENDING && updateEventAdminRequest.getStateAction() == StateAction.REJECT_EVENT) {
            eventFromRepo.setState(State.CANCELED);
            eventFromRepo.setPublishedOn(null);
        }

        eventFromRepo = eventRepository.save(eventFromRepo);

        return EventMapper.toEventFullDto(eventFromRepo);
    }
}