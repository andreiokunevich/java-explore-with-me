package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.interfaces.ParticipationRequestService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllParticipationRequests(@PathVariable Long userId) {
        return participationRequestService.getAllRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createParticipationRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return participationRequestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Long userId,
                                                              @PathVariable Long requestId) {
        return participationRequestService.cancelRequest(userId, requestId);
    }
}