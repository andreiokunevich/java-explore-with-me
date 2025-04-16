package ru.practicum;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseClient {

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <T> ResponseEntity<T> get(String path,
                                        @Nullable Map<String, Object> parameters,
                                        ParameterizedTypeReference<T> responseType) {
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null, responseType);
    }

    protected <T, R> ResponseEntity<R> post(String path, T body, ParameterizedTypeReference<R> responseType) {
        return makeAndSendRequest(HttpMethod.POST, path, null, body, responseType);
    }

    private <T, R> ResponseEntity<R> makeAndSendRequest(HttpMethod method,
                                                        String path,
                                                        @Nullable Map<String, Object> parameters,
                                                        @Nullable T body,
                                                        ParameterizedTypeReference<R> responseType) {
        HttpEntity<T> requestEntity = (body == null) ? null : new HttpEntity<>(body);
        try {
            ResponseEntity<R> response = (parameters != null)
                    ? rest.exchange(path, method, requestEntity, responseType, parameters)
                    : rest.exchange(path, method, requestEntity, responseType);
            return prepareResponse(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    private static <R> ResponseEntity<R> prepareResponse(ResponseEntity<R> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}