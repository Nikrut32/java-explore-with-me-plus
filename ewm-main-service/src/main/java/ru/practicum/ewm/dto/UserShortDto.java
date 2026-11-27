package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortDto {

    @NotNull(message = "Идентификатор пользователя не должен быть пустым")
    private Long id;

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String name;
}