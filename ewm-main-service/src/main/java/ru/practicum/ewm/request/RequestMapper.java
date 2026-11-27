package ru.practicum.ewm.request;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder().id(request.getId()).created(request.getCreated()).event(request.getEvent().getId()).requester(request.getRequester().getId()).status(request.getStatus()).build();
    }
}