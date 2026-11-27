package ru.practicum.ewm.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

@RestController
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @RequestParam @Positive(message = "Идентификатор события должен быть положительным") Long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @PathVariable @Positive(message = "Идентификатор запроса должен быть положительным") Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @PathVariable @Positive(message = "Идентификатор события должен быть положительным") Long eventId) {
        return requestService.getEventParticipants(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @PathVariable @Positive(message = "Идентификатор события должен быть положительным") Long eventId, @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return requestService.changeRequestStatus(userId, eventId, updateRequest);
    }
}