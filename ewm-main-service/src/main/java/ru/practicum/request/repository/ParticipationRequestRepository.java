package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.enums.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    ParticipationRequest findByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findRequestByIdIn(List<Long> requestsId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);
}