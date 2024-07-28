package ru.yandex.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.enums.EventSortType;
import ru.yandex.practicum.model.enums.RatingSortType;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getAllEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd, Pageable pageable);

    EventFullDto getEventById(Long eventId);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequestDto eventDto);

    List<EventShortDto> getAllEventsSorted(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Boolean onlyAvailable, EventSortType sort,
                                           Pageable pageable, HttpServletRequest request);

    List<EventShortDto> getAllEventsCreatedByUser(Long userId, Pageable pageable);

    EventFullDto addEvent(Long userId, NewEventDto eventDto);

    EventFullDto getEventByIdCreatedByUser(Long userId, Long eventId);

    EventFullDto updateEventCreatedByUser(Long userId, Long eventId, UpdateEventUserRequestDto eventDto);

    List<EventShortDto> getAllEventsSortedByRating(RatingSortType sort, Pageable pageable, HttpServletRequest request);
}
