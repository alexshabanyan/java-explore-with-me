package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.enums.EventSortType;
import ru.yandex.practicum.service.EventService;
import ru.yandex.practicum.service.RequestService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventController {
    private final EventService eventService;
    private final RequestService requestService;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // ---------------admin------------------
    @GetMapping("/admin/events")
    public List<EventFullDto> getAllEvents(@RequestParam(required = false) List<Long> users,
                                           @RequestParam(required = false) List<String> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeStart,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeEnd,
                                           @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                           @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Получен GET запрос на нахождение всех событий с параметрами: users= {}; states= {}; categories= {}; " +
                        "rangeStart= {}; rangeEnd= {}; from= {}; size= {}.", users, states, categories, rangeStart,
                rangeEnd, from, size);
        int page = from > 0 ? from / size : from;
        return eventService.getAllEvents(users, states, categories, rangeStart, rangeEnd, PageRequest.of(page, size));
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequestDto eventDto) {
        log.info("Получен PATCH запрос на обновление события с ID: {}. Было:\n{}\n Стало:\n {}",
                eventId, eventService.getEventById(eventId), eventDto);
        return eventService.updateEvent(eventId, eventDto);
    }

    // ---------------public-----------------
    @GetMapping("/events")
    public List<EventShortDto> getAllEventsSorted(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSortType sort,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size,
            HttpServletRequest request) {
        log.info("Получен GET запрос на нахождение всех событий с параметрами: text= {}; categories= {}; paid= {}; " +
                        "rangeStart= {}; rangeEnd= {}; onlyAvailable= {}; sort= {}; from= {}; size= {}.", text,
                categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        int page = from > 0 ? from / size : from;
        PageRequest pageRequest = PageRequest.of(page, size);
        if (sort != null) {
            if (sort.equals(EventSortType.EVENT_DATE)) {
                pageRequest = PageRequest.of(page, size, Sort.by("eventDate"));
            } else {
                pageRequest = PageRequest.of(page, size, Sort.by("views").descending());
            }
        }
        return eventService.getAllEventsSorted(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                pageRequest, request);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEventById(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("Получен GET запрос на нахождение события по ID: {}.", id);
        return eventService.getEventById(id, request);
    }

    // ---------------private----------------
    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getAllEventsCreatedByUser(@PathVariable Long userId,
                                                         @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                         @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Получен GET запрос на нахождение всех событий, созданных пользователем: {} с параметрами: from= {}; " +
                "size= {}.", userId, from, size);
        int page = from > 0 ? from / size : from;
        return eventService.getAllEventsCreatedByUser(userId, PageRequest.of(page, size));
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto eventDto) {
        log.info("Получен POST запрос на добавление нового события: {} пользователем : {}", eventDto, userId);
        return eventService.addEvent(userId, eventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventByIdCreatedByUser(@PathVariable Long userId,
                                                  @PathVariable Long eventId) {
        log.info("Получен GET запрос на нахождение события по ID: {}, созданного пользователем: {}.", eventId, userId);
        return eventService.getEventByIdCreatedByUser(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventCreatedByUser(@PathVariable Long userId, @PathVariable Long eventId,
                                                 @Valid @RequestBody UpdateEventUserRequestDto eventDto) {
        log.info("Получен PATCH запрос на обновление события с ID: {}, созданного пользователем: {}. Было:\n{}\n Стало:\n {}",
                eventId, userId, eventService.getEventById(eventId), eventDto);
        return eventService.updateEventCreatedByUser(userId, eventId, eventDto);

    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestsForEventCreatedByUser(@PathVariable Long userId,
                                                                             @PathVariable Long eventId) {
        log.info("Получен GET запрос на нахождение запросов в событии по ID: {}, пользователя: {}.", eventId, userId);
        return requestService.getAllRequestsForEventCreatedByUser(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateRequestsStatusForEventCreatedByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequestDto eventDto) {
        log.info("Получен PATCH запрос на обновление статуса заявок на участие в событии с ID: {}, созданного пользователем: {}. " +
                "Стало:\n {}", eventId, userId, eventDto);
        return requestService.updateRequestsStatusForEventCreatedByUser(userId, eventId, eventDto);
    }
}
