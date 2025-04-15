package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(
            List<Long> userIds,
            List<State> states,
            List<Long> categoryIds,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM Event e WHERE (:text IS NULL OR (e.annotation ILIKE %:text% OR e.description ILIKE %:text%))
        AND (:categories IS NULL OR e.category.id IN :categories)
        AND (:paid IS NULL OR e.paid = :paid)
        AND (e.eventDate >= :start)
        AND (CAST(:end AS DATE) IS NULL OR e.eventDate <= :end)
        AND (:onlyAvailable = false OR e.participantLimit = 0 OR e.confirmedRequests <= e.participantLimit)
        ORDER BY e.eventDate DESC
    """)
    Page<Event> searchEvents(@Param("text") String text,
                             @Param("categories") List<Long> categories,
                             @Param("paid") Boolean paid,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("onlyAvailable") boolean onlyAvailable,
                             Pageable pageable);

    List<Event> findByIdIn(List<Long> ids);

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Boolean existsByCategoryId(Long catId);
}
