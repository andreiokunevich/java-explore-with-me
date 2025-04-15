package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl,
                       RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<EndpointHitDto> createHit(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto, new ParameterizedTypeReference<>() {
        });
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start != null ? start : "",
                "end", end != null ? end : "",
                "uris", uris != null ? String.join(",", uris) : "",
                "unique", unique != null ? unique : false
        );

        return get("/stats?start={start}&end={end}&unique={unique}&uris={uris}", parameters,
                new ParameterizedTypeReference<>() {
                });
    }
}