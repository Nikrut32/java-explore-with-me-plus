package ru.practicum.ewm.request.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.request.model.RequestUpdateStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Список идентификаторов заявок не должен быть пустым")
    private List<Long> requestIds;

    @NotNull(message = "Новый статус заявок не должен быть пустым")
    private RequestUpdateStatus status;
}