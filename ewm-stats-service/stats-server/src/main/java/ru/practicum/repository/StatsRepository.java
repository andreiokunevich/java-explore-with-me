package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.ViewStatsDto(eh.ip, eh.uri, COUNT(DISTINCT eh.ip))" +
            " FROM EndpointHit AS eh WHERE eh.timestamp BETWEEN :start AND :end AND (:uris IS NULL OR eh.uri IN :uris) " +
            " GROUP BY eh.ip, eh.uri" +
            " ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStatsDto> findAllWithUniqueIpTrue(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.ViewStatsDto(eh.ip, eh.uri, COUNT(eh.ip))" +
            " FROM EndpointHit AS eh WHERE eh.timestamp BETWEEN :start AND :end AND (:uris IS NULL OR eh.uri IN :uris)" +
            " GROUP BY eh.ip, eh.uri" +
            " ORDER BY COUNT(eh.ip) DESC")
    List<ViewStatsDto> findAllWithUniqueIpFalse(LocalDateTime start, LocalDateTime end, List<String> uris);
}