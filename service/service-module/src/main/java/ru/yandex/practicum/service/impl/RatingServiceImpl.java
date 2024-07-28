package ru.yandex.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.exceptions.ValidationException;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.Rating;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.dto.EventFullDto;
import ru.yandex.practicum.model.dto.RatingDto;
import ru.yandex.practicum.model.enums.RatingType;
import ru.yandex.practicum.model.enums.RequestStatus;
import ru.yandex.practicum.model.mapper.EventMapper;
import ru.yandex.practicum.model.mapper.RatingMapper;
import ru.yandex.practicum.repository.*;
import ru.yandex.practicum.service.RatingService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final RatingRepository ratingRepository;

    @SneakyThrows
    @Transactional
    @Override
    public EventFullDto addRating(Long userId, Long eventId, RatingDto ratingDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        List<Request> confirmedRequests =
                requestRepository.findAllByRequesterIdAndEventIdAndStatus(userId, eventId, RequestStatus.CONFIRMED);

        if (confirmedRequests.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + " не может поставить рейтинг не посещенному событию "
                    + eventId + ".");
        }

        List<Rating> existingRatings =
                ratingRepository.findAllByEventIdAndUserId(eventId, userId);

        if (!existingRatings.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + " уже ставил рейтинг " + RatingType.valueOf(ratingDto.getRating())
                    + " событию " + eventId + ".");
        }

        Rating rating = RatingMapper.toRating(ratingDto);
        rating.setEvent(event);
        rating.setUser(user);

        Rating ratingDb = ratingRepository.save(rating);

        log.info("Рейтинг добавлен в базу данных в таблицу events_rating по ID: {} \n {}", ratingDb.getId(), ratingDb);
        Event eventUpd = updateRatingForEvent(event);
        updateRatingForUser(event);
        return EventMapper.toEventFullDto(eventUpd);
    }

    @SneakyThrows
    @Override
    @Transactional
    public EventFullDto updateRating(Long userId, Long eventId, Long ratingId, RatingDto ratingDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        Rating ratingOld = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Рейтинг по ID: " + ratingId + " не найден."));

        List<Request> confirmedRequests =
                requestRepository.findAllByRequesterIdAndEventIdAndStatus(userId, eventId, RequestStatus.CONFIRMED);

        if (confirmedRequests.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + " не может поставить рейтинг не посещенному событию "
                    + eventId + ".");
        }

        List<Rating> existingSameRatings =
                ratingRepository.findAllByEventIdAndUserIdAndRating(eventId, userId, RatingType.valueOf(ratingDto.getRating()));

        if (!existingSameRatings.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + " уже ставил рейтинг " + RatingType.valueOf(ratingDto.getRating())
                    + " событию " + eventId + ".");
        }

        ratingOld.setRating(RatingType.valueOf(ratingDto.getRating()));
        Rating ratingUpb = ratingRepository.save(ratingOld);

        log.info("Рейтинг обновлен в базе данных в таблице events_rating по ID: {} \n {}.", ratingUpb.getId(), ratingUpb);
        Event eventUpd = updateRatingForEvent(event);
        updateRatingForUser(event);
        return EventMapper.toEventFullDto(eventUpd);
    }

    @SneakyThrows
    @Override
    @Transactional
    public EventFullDto deleteRating(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        List<Rating> ratingsEvent = ratingRepository.findAllByEventId(eventId);

        if (ratingsEvent.isEmpty()) {
            throw new ValidationException("Рейтинг для события " + eventId + " пуст.");
        }

        List<Long> ratingIds = ratingsEvent.stream()
                .map(Rating::getId)
                .collect(Collectors.toList());
        ratingRepository.deleteByIdIn(ratingIds);
        log.info("Рейтинг удален из базы данных из таблицы events_rating по ID: {}", ratingIds);
        Event eventUpd = updateRatingForEvent(event);
        updateRatingForUser(event);
        return EventMapper.toEventFullDto(eventUpd);
    }

    private Event updateRatingForEvent(Event event) {
        Long likesAmount = ratingRepository.countRatingByEventIdAndRating(event.getId(), RatingType.LIKE.name());
        Long dislikesAmount = ratingRepository.countRatingByEventIdAndRating(event.getId(), RatingType.DISLIKE.name());

        event.setRating(likesAmount - dislikesAmount);
        Event eventUpd = eventRepository.save(event);
        log.info("Событие обновлено в базе данных в таблице events по ID: {}. Был рейтинг: {}; Стал рейтинг: {}.",
                eventUpd.getId(), event.getRating(), eventUpd.getRating());
        return eventUpd;
    }

    private void updateRatingForUser(Event event) {
        User initiator = userRepository.findById(event.getInitiator().getId()).get();
        Long oldRating = initiator.getRating();

        Long likesAmount = ratingRepository.countRatingByUserIdAndRating(initiator.getId(), RatingType.LIKE.name());
        Long dislikesAmount = ratingRepository.countRatingByEventIdAndRating(initiator.getId(), RatingType.DISLIKE.name());

        initiator.setRating(likesAmount - dislikesAmount);

        User userUpd = userRepository.save(initiator);
        log.info("Рейтинг пользователя обновлен в базе данных в таблице users по ID: {}. Был рейтинг: {}; Стал рейтинг: {}.",
                userUpd.getId(), oldRating, userUpd.getRating());
    }

}
