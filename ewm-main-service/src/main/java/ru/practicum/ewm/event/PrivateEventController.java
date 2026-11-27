package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Параметр from не может быть отрицательным") int from, @RequestParam(defaultValue = "10") @Positive(message = "Параметр size должен быть положительным") int size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @PathVariable @Positive(message = "Идентификатор события должен быть положительным") Long eventId) {
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable @Positive(message = "Идентификатор пользователя должен быть положительным") Long userId, @PathVariable @Positive(message = "Идентификатор события должен быть положительным") Long eventId, @RequestBody @Valid UpdateEventUserRequest updateRequest) {
        return eventService.updateUserEvent(userId, eventId, updateRequest);
    }
}