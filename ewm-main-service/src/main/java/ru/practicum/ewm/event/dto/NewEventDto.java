package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {

    @NotBlank(message = "Краткое описание не должно быть пустым")
    @Size(min = 20, max = 2000, message = "Краткое описание должно содержать от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "Категория не должна быть пустой")
    @Positive(message = "Идентификатор категории должен быть положительным")
    private Long category;

    @NotBlank(message = "Полное описание не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Полное описание должно содержать от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Дата события не должна быть пустой")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Локация не должна быть пустой")
    @Valid
    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    @Builder.Default
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок не должен быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    private String title;
}