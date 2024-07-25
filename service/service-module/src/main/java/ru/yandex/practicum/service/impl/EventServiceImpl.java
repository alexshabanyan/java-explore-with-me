package ru.yandex.practicum.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.exceptions.ConflictException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.exceptions.ValidationException;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.enums.*;
import ru.yandex.practicum.model.mapper.EventMapper;
import ru.yandex.practicum.repository.*;
import ru.yandex.practicum.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getAllEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Pageable pageable) {

        Specification<Event> specification = Specification.where(null);
        if (users != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> root.get("initiator").get("id").in(users));
        }
        if (states != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> root.get("state").as(String.class).in(states));
        }
        if (categories != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> root.get("category").get("id").in(categories));
        }
        if (rangeStart != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                            root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"),
                            rangeEnd));
        }


        List<Event> events = eventRepository.findAll(specification, pageable);
        List<EventFullDto> eventsRes = new ArrayList<>();

        for (Event event : events) {
            EventFullDto eventDto = EventMapper.toEventFullDto(event);
            eventsRes.add(eventDto);
        }

        log.info("Получено {} событий из базы данных из таблицы events.", eventsRes.size());
        return eventsRes;
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));
        log.info("Событие по ID: {} получено из базы данных: {}.", eventId, event);
        return EventMapper.toEventFullDto(event);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие по ID: " + eventId + " не найдено в опубликованных событиях.");
        }

        String uri = request.getRequestURI();
        statsClient.hit(uri, request.getRemoteAddr(), LocalDateTime.now());

        ResponseEntity<Object> response = statsClient.getStats(event.getCreatedOn(),
                event.getEventDate(), new String[]{uri}, true);

        List<EventViewStats> list = objectMapper.readValue(
                objectMapper.writeValueAsString(response.getBody()), new TypeReference<>() {
                });

        if (!list.isEmpty()) {
            event.setViews(Long.valueOf(list.get(0).getHits()));
        }

        log.info("Событие по ID: {} получено из базы данных: {}.", eventId, event);
        return EventMapper.toEventFullDto(event);
    }

    @SneakyThrows
    @Transactional
    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequestDto eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        if (!event.getState().equals(EventState.PENDING)) {
            throw new ConflictException("Событие по ID: " + eventId + " не в состоянии ожидания: " + event.getState() + ".");
        }

        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория по ID: " + eventDto.getCategory() + " не найдена."));
            event.setCategory(category);
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события в прошлом: " + eventDto.getEventDate());
            }
            if (checkEventDateDuration(eventDto.getEventDate())) {
                throw new ConflictException("Дата события раньше, чем через час от даты публикации: " + eventDto.getEventDate());
            }
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(objectMapper.writeValueAsString(eventDto.getLocation()));
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getStateAction() != null) {
            EventState state = eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)
                    ? EventState.PUBLISHED
                    : EventState.CANCELED;
            event.setState(state);
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }

        Event eventUpd = eventRepository.save(event);
        log.info("Событие обновлено в базе данных в таблице events по ID: {} \n {}", eventId, eventUpd);
        return EventMapper.toEventFullDto(eventUpd);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventsSorted(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Boolean onlyAvailable, EventSortType sort,
                                                  Pageable pageable, HttpServletRequest request) {
        Specification<Event> specification = Specification.where(null);

        specification = specification.and(
                (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"),
                        EventState.PUBLISHED));

        if (text != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                            "%" + text.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                            "%" + text.toLowerCase() + "%")));
        }
        if (categories != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), paid));
        }
        LocalDateTime startDateTime = Objects.requireNonNullElse(rangeStart, LocalDateTime.now());
        specification = specification.and(
                (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("eventDate"),
                        startDateTime));

        if (rangeEnd != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("eventDate"),
                            rangeEnd));

            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException("Дата начала поиска (" + rangeStart + ") позже даты конца поиска (" + rangeEnd + ").");
            }
        }

        if (onlyAvailable != null && onlyAvailable) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                            root.get("participantLimit"), 0));
        }

        List<Event> events = eventRepository.findAll(specification, pageable);
        statsClient.hit(request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        ResponseEntity<Object> response = getEventsViewStats(events);
        List<EventViewStats> list = objectMapper.readValue(
                objectMapper.writeValueAsString(response.getBody()),
                new TypeReference<>() {
                });

        Map<Long, Long> eventsHits = getEventsHits(list);

        events.forEach(event -> event.setViews(eventsHits.get(event.getId())));

        List<EventShortDto> eventsRes = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        log.info("Получено {} событий из базы данных из таблицы events.", eventsRes.size());
        return eventsRes;
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventsSortedByRating(RatingSortType sort, Pageable pageable, HttpServletRequest request) {
        List<Event> events = new ArrayList<>();
        switch (sort) {
            case ASC:
                events = eventRepository.findAll(pageable).stream()
                        .sorted(Comparator.comparing(Event::getRating).thenComparing(Event::getId))
                        .collect(Collectors.toList());
                break;
            case DESC:
                events = eventRepository.findAll(pageable).stream()
                        .sorted(Comparator.comparing(Event::getRating).reversed().thenComparing(Event::getId))
                        .collect(Collectors.toList());
                break;
        }

        statsClient.hit(request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        ResponseEntity<Object> response = getEventsViewStats(events);
        List<EventViewStats> list = objectMapper.readValue(
                objectMapper.writeValueAsString(response.getBody()),
                new TypeReference<>() {
                });

        Map<Long, Long> eventsHits = getEventsHits(list);

        events.forEach(event -> event.setViews(eventsHits.get(event.getId())));

        List<EventShortDto> eventsRes = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        log.info("Получено {} событий из базы данных из таблицы events.", eventsRes.size());
        return eventsRes;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventsCreatedByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        List<EventShortDto> events = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        log.info("Получено {} событий из базы данных из таблицы events.", events.size());
        return events;
    }

    @SneakyThrows
    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto eventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        if (eventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата события в прошлом: " + eventDto.getEventDate());
        }
        if (checkEventDateDuration(eventDto.getEventDate())) {
            throw new ConflictException("Дата события раньше, чем через час от даты публикации: " + eventDto.getEventDate());
        }

        Category category = categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория по ID: " + eventDto.getCategory() + " не найдена."));

        Event event = EventMapper.toEvent(eventDto, category);

        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setRating(0L);

        Event eventDb = eventRepository.save(event);
        log.info("Событие добавлено в базу данных в таблицу events по ID: {} \n {}", eventDb.getId(), eventDb);
        return EventMapper.toEventFullDto(eventDb);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByIdCreatedByUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException("Событие по ID: " + eventId + " с инициатором " + userId + " не найдено.");
        }

        log.info("Событие по ID: {} с инициатором: {} получено из базы данных: {}.", eventId, userId, event);
        return EventMapper.toEventFullDto(event);
    }

    @SneakyThrows
    @Transactional
    @Override
    public EventFullDto updateEventCreatedByUser(Long userId, Long eventId, UpdateEventUserRequestDto eventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException("Событие по ID: " + eventId + " с инициатором " + userId + " не найдено.");
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие по ID: " + eventId + " уже опубликованно.");

        }

        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория по ID: " + eventDto.getCategory() + " не найдена."));
            event.setCategory(category);
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события в прошлом: " + eventDto.getEventDate());
            }
            if (checkEventDateDuration(eventDto.getEventDate())) {
                throw new ConflictException("Дата события раньше, чем через час от даты публикации: " + eventDto.getEventDate());
            }
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(objectMapper.writeValueAsString(eventDto.getLocation()));
        }
        if (eventDto.getStateAction() != null) {
            EventState status = eventDto.getStateAction().equals(RequestStateAction.CANCEL_REVIEW)
                    ? EventState.CANCELED
                    : EventState.PENDING;
            event.setState(status);
        }

        Event eventUpd = eventRepository.save(event);
        log.info("Событие обновлено в базе данных в таблице events по ID: {} \n {}", eventId, eventUpd);
        return EventMapper.toEventFullDto(eventUpd);
    }

    private boolean checkEventDateDuration(LocalDateTime eventDate) {
        return Duration.between(LocalDateTime.now(), eventDate).toHours() < 2L;
    }

    private ResponseEntity<Object> getEventsViewStats(List<Event> events) {
        String[] uri = getStatsUrisByEvents(events);
        LocalDateTime start = getStartDateTimeForStatsView(events);
        return statsClient.getStats(start, LocalDateTime.now(), uri, true);
    }

    private LocalDateTime getStartDateTimeForStatsView(List<Event> events) {
        return events.stream()
                .map(Event::getCreatedOn)
                .sorted()
                .findFirst()
                .orElseThrow();
    }

    private String[] getStatsUrisByEvents(List<Event> events) {
        return events.stream()
                .map(it -> "/events/" + it.getId())
                .toArray(String[]::new);
    }

    private Map<Long, Long> getEventsHits(List<EventViewStats> list) {
        Map<Long, Long> hits = new HashMap<>();
        for (EventViewStats eventViewStats : list) {
            String[] temp = eventViewStats.getUri().split("/");
            Long eventId = Long.parseLong(temp[temp.length - 1]);
            hits.put(eventId, Long.valueOf(eventViewStats.getHits()));
        }
        return hits;
    }
}
