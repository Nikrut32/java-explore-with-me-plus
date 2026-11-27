package ru.practicum.ewm.event;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.LocationDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;

@Component
public class EventMapper {

    public Event toEntity(NewEventDto dto, Category category, User initiator, LocalDateTime now) {
        Location location = Location.builder().lat(dto.getLocation().getLat()).lon(dto.getLocation().getLon()).build();

        return Event.builder().annotation(dto.getAnnotation()).category(category).createdOn(now).description(dto.getDescription()).eventDate(dto.getEventDate()).initiator(initiator).location(location).paid(dto.getPaid() != null ? dto.getPaid() : false).participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0).requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true).state(EventState.PENDING).title(dto.getTitle()).build();
    }

    public EventFullDto toFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder().id(event.getId()).annotation(event.getAnnotation()).category(CategoryDto.builder().id(event.getCategory().getId()).name(event.getCategory().getName()).build()).confirmedRequests(confirmedRequests).createdOn(event.getCreatedOn()).description(event.getDescription()).eventDate(event.getEventDate()).initiator(UserShortDto.builder().id(event.getInitiator().getId()).name(event.getInitiator().getName()).build()).location(LocationDto.builder().lat(event.getLocation().getLat()).lon(event.getLocation().getLon()).build()).paid(event.getPaid()).participantLimit(event.getParticipantLimit()).publishedOn(event.getPublishedOn()).requestModeration(event.getRequestModeration()).state(event.getState()).title(event.getTitle()).views(views).build();
    }

    public EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder().id(event.getId()).annotation(event.getAnnotation()).category(CategoryDto.builder().id(event.getCategory().getId()).name(event.getCategory().getName()).build()).confirmedRequests(confirmedRequests).eventDate(event.getEventDate()).initiator(UserShortDto.builder().id(event.getInitiator().getId()).name(event.getInitiator().getName()).build()).paid(event.getPaid()).title(event.getTitle()).views(views).build();
    }
}