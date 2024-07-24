package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.dto.ParticipationRequestDto;
import ru.yandex.practicum.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class RequestController {
    private final RequestService requestService;

    // ---------------private----------------
    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getAllRequestsByUser(@PathVariable("userId") Long userId) {
        log.info("Получен GET запрос на нахождение всех запросов пользователя с ID: {}.", userId);
        return requestService.getAllRequestsByUser(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequestByUser(@PathVariable("userId") Long userId,
                                                    @RequestParam Long eventId) {
        log.info("Получен POST запрос на добавление нового запроса пользователем: {} в событии: {}.", userId, eventId);
        return requestService.addRequestByUser(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequestByIdUserId(@PathVariable("userId") Long userId,
                                                           @PathVariable("requestId") Long requestId) {
        log.info("Получен PATCH запрос на отмену участия пользователя с ID: {} в запросе: {}", userId, requestId);
        return requestService.cancelRequestByIdUserId(userId, requestId);
    }
}
