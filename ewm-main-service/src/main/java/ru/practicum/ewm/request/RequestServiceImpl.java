package ru.practicum.ewm.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.RequestUpdateStatus;
import ru.practicum.ewm.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        findUserOrThrow(userId);
        return requestRepository.findAllByRequesterId(userId).stream().map(requestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User requester = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие в своём событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя подать заявку на участие в неопубликованном событии");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка на участие в событии с id=" + eventId + " уже существует");
        }

        int limit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
        if (limit > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= limit) {
                throw new ConflictException("Достигнут лимит заявок на участие в событии с id=" + eventId);
            }
        }

        boolean moderationRequired = Boolean.TRUE.equals(event.getRequestModeration()) && limit > 0;
        RequestStatus initialStatus = moderationRequired ? RequestStatus.PENDING : RequestStatus.CONFIRMED;

        ParticipationRequest request = ParticipationRequest.builder().created(LocalDateTime.now()).event(event).requester(requester).status(initialStatus).build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Создана заявка id={} пользователя userId={} на событие eventId={} со статусом {}", saved.getId(), userId, eventId, saved.getStatus());

        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        findUserOrThrow(userId);
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " у пользователя с id=" + userId + " не найдена"));

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        log.info("Заявка id={} отменена пользователем userId={}", requestId, userId);

        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        findUserOrThrow(userId);
        findUserEventOrThrow(userId, eventId);

        return requestRepository.findAllByEventId(eventId).stream().map(requestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        findUserOrThrow(userId);
        Event event = findUserEventOrThrow(userId, eventId);

        List<ParticipationRequest> targetRequests = requestRepository.findAllByEventIdAndIdIn(eventId, updateRequest.getRequestIds());

        for (ParticipationRequest req : targetRequests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Изменить статус можно только у заявок в состоянии ожидания (id=" + req.getId() + ")");
            }
        }

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        if (updateRequest.getStatus() == RequestUpdateStatus.REJECTED) {
            for (ParticipationRequest req : targetRequests) {
                req.setStatus(RequestStatus.REJECTED);
                requestRepository.save(req);
                rejectedDtos.add(requestMapper.toDto(req));
            }
            return EventRequestStatusUpdateResult.builder().confirmedRequests(confirmedDtos).rejectedRequests(rejectedDtos).build();
        }

        int limit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (limit > 0 && confirmedCount >= limit) {
            throw new ConflictException("Достигнут лимит одобренных заявок для события с id=" + eventId);
        }

        for (ParticipationRequest req : targetRequests) {
            if (limit == 0 || confirmedCount < limit) {
                req.setStatus(RequestStatus.CONFIRMED);
                requestRepository.save(req);
                confirmedDtos.add(requestMapper.toDto(req));
                confirmedCount++;
            } else {
                req.setStatus(RequestStatus.REJECTED);
                requestRepository.save(req);
                rejectedDtos.add(requestMapper.toDto(req));
            }
        }

        if (limit > 0 && confirmedCount >= limit) {
            List<ParticipationRequest> remainingPending = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (ParticipationRequest pending : remainingPending) {
                pending.setStatus(RequestStatus.REJECTED);
                requestRepository.save(pending);
                rejectedDtos.add(requestMapper.toDto(pending));
            }
        }

        return EventRequestStatusUpdateResult.builder().confirmedRequests(confirmedDtos).rejectedRequests(rejectedDtos).build();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private Event findUserEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " у пользователя с id=" + userId + " не найдено"));
    }
}