package ru.yandex.practicum.model.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.Location;
import ru.yandex.practicum.model.dto.EventFullDto;
import ru.yandex.practicum.model.dto.EventShortDto;
import ru.yandex.practicum.model.dto.NewEventDto;

@UtilityClass
public class EventMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static EventFullDto toEventFullDto(Event event) throws JsonProcessingException {
        if (event != null) {
            return EventFullDto.builder()
                    .annotation(event.getAnnotation())
                    .category(CategoryMapper.toCategoryDto(event.getCategory()))
                    .confirmedRequests(event.getConfirmedRequests())
                    .createdOn(event.getCreatedOn())
                    .description(event.getDescription())
                    .eventDate(event.getEventDate())
                    .id(event.getId())
                    .initiator(UserMapper.toUserDto(event.getInitiator()))
                    .location(LocationMapper.toLocationDto(objectMapper.readValue(event.getLocation(), Location.class)))
                    .paid(event.getPaid())
                    .participantLimit(event.getParticipantLimit())
                    .publishedOn(event.getPublishedOn())
                    .requestModeration(event.isRequestModeration())
                    .state(event.getState().name())
                    .title(event.getTitle())
                    .views(event.getViews())
                    .rating(event.getRating())
                    .build();
        } else {
            return null;
        }
    }

    public static EventShortDto toEventShortDto(Event event) {
        if (event != null) {
            return EventShortDto.builder()
                    .annotation(event.getAnnotation())
                    .category(CategoryMapper.toCategoryDto(event.getCategory()))
                    .confirmedRequests(event.getConfirmedRequests())
                    .eventDate(event.getEventDate())
                    .id(event.getId())
                    .initiator(UserMapper.toUserDto(event.getInitiator()))
                    .paid(event.getPaid())
                    .title(event.getTitle())
                    .views(event.getViews())
                    .rating(event.getRating())
                    .build();
        } else {
            return null;
        }
    }

    public static Event toEvent(NewEventDto eventDto, Category category) throws JsonProcessingException {
        if (eventDto != null) {
            return Event.builder()
                    .annotation(eventDto.getAnnotation())
                    .category(category)
                    .description(eventDto.getDescription())
                    .eventDate(eventDto.getEventDate())
                    .location(objectMapper.writeValueAsString(eventDto.getLocation()))
                    .paid(eventDto.getPaid() != null ? eventDto.getPaid() : false)
                    .participantLimit(eventDto.getParticipantLimit() != null ? eventDto.getParticipantLimit() : 0)
                    .requestModeration(eventDto.getRequestModeration() != null ? eventDto.getRequestModeration() : true)
                    .title(eventDto.getTitle())
                    .views(0L)
                    .rating(0L)
                    .confirmedRequests(0)
                    .build();
        } else {
            return null;
        }
    }
}
