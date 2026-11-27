package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.dto.LocationDto;
import ru.practicum.ewm.event.model.UserStateAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Краткое описание должно содержать от 20 до 2000 символов")
    private String annotation;

    @Positive(message = "Идентификатор категории должен быть положительным")
    private Long category;

    @Size(min = 20, max = 7000, message = "Полное описание должно содержать от 20 до 7000 символов")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Valid
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    private Boolean requestModeration;

    private UserStateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    private String title;
}