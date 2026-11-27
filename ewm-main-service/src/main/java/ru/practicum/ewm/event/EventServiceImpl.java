package ru.practicum.ewm.event;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.UserStateAction;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.request.RequestRepository;
import ru.practicum.ewm.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long MIN_HOURS_BEFORE_EVENT = 2L;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        findUserOrThrow(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest).getContent();
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedMap = getConfirmedCounts(eventIds);
        Map<Long, Long> viewsMap = getViewsCounts(eventIds);

        return events.stream().map(event -> eventMapper.toShortDto(event, confirmedMap.getOrDefault(event.getId(), 0L), viewsMap.getOrDefault(event.getId(), 0L))).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = findUserOrThrow(userId);
        Category category = findCategoryOrThrow(newEventDto.getCategory());

        LocalDateTime now = LocalDateTime.now();
        validateEventDate(newEventDto.getEventDate(), now);

        Event event = eventMapper.toEntity(newEventDto, category, initiator, now);
        Event saved = eventRepository.save(event);
        log.info("Создано событие id={} пользователем userId={}", saved.getId(), userId);

        return eventMapper.toFullDto(saved, 0L, 0L);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        findUserOrThrow(userId);
        Event event = findUserEventOrThrow(userId, eventId);

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = getViewsCounts(List.of(eventId)).getOrDefault(eventId, 0L);

        return eventMapper.toFullDto(event, confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        findUserOrThrow(userId);
        Event event = findUserEventOrThrow(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменять можно только отмененные события или события в состоянии ожидания модерации");
        }

        LocalDateTime now = LocalDateTime.now();
        if (updateRequest.getEventDate() != null) {
            validateEventDate(updateRequest.getEventDate(), now);
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategory(findCategoryOrThrow(updateRequest.getCategory()));
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getLocation() != null) {
            event.getLocation().setLat(updateRequest.getLocation().getLat());
            event.getLocation().setLon(updateRequest.getLocation().getLon());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = getViewsCounts(List.of(eventId)).getOrDefault(eventId, 0L);

        return eventMapper.toFullDto(updated, confirmed, views);
    }

    private void validateEventDate(LocalDateTime eventDate, LocalDateTime now) {
        if (eventDate.isBefore(now.plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new ConflictException("Дата и время начала события должны быть не раньше чем через 2 часа от текущего момента");
        }
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Категория с id=" + categoryId + " не найдена"));
    }

    private Event findUserEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " у пользователя с id=" + userId + " не найдено"));
    }

    private Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        Map<Long, Long> result = new HashMap<>();
        for (RequestRepository.ConfirmedCountProjection row : requestRepository.countConfirmedByEventIds(eventIds, RequestStatus.CONFIRMED)) {
            result.put(row.getEventId(), row.getCnt());
        }
        return result;
    }

    private Map<Long, Long> getViewsCounts(List<Long> eventIds) {
        Map<Long, Long> viewsByEventId = new HashMap<>();
        if (eventIds.isEmpty()) {
            return viewsByEventId;
        }
        List<String> uris = eventIds.stream().map(id -> "/events/" + id).collect(Collectors.toList());
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusYears(50);
            LocalDateTime end = now.plusYears(50);
            ResponseEntity<Object> response = statsClient.getStats(start, end, uris, Boolean.TRUE);
            if (response != null && response.getBody() != null) {
                ViewStats[] stats = objectMapper.convertValue(response.getBody(), ViewStats[].class);
                for (ViewStats item : stats) {
                    if (item.getUri() != null && item.getUri().startsWith("/events/")) {
                        String idPart = item.getUri().substring("/events/".length());
                        try {
                            Long eventId = Long.parseLong(idPart);
                            viewsByEventId.put(eventId, item.getHits());
                        } catch (NumberFormatException ignored) {
                            log.debug("Не удалось распарсить id события из uri: {}", item.getUri());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Не удалось получить статистику просмотров из сервиса статистики: {}", e.getMessage());
        }
        return viewsByEventId;
    }
}