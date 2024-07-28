package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Rating;
import ru.yandex.practicum.model.enums.RatingType;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query(value = "select count(distinct user_id) as cnt " +
            "from events_rating er " +
            "where event_id = ?1 " +
            "and rating = ?2", nativeQuery = true)
    Long countRatingByEventIdAndRating(Long eventId, String ratingType);

    @Query(value = "select count(distinct er.user_id) as cnt " +
            "from events_rating er " +
            "join events e on e.id = er.event_id " +
            "where e.user_id = ?1 " +
            "and er.rating = ?2", nativeQuery = true)
    Long countRatingByUserIdAndRating(Long userId, String ratingType);

    List<Rating> findAllByEventIdAndUserIdAndRating(Long eventId, Long userId, RatingType ratingType);

    List<Rating> findAllByEventIdAndUserId(Long eventId, Long userId);

    List<Rating> findAllByEventId(Long eventId);

    void deleteByIdIn(List<Long> ids);
}
