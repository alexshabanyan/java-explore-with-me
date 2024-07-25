package ru.yandex.practicum.service;

import ru.yandex.practicum.model.dto.EventFullDto;
import ru.yandex.practicum.model.dto.RatingDto;

public interface RatingService {

    EventFullDto addRating(Long userId, Long eventId, RatingDto ratingDto);

    EventFullDto updateRating(Long userId, Long eventId, Long ratingId, RatingDto ratingDto);

    EventFullDto deleteRating(Long eventId);
}
