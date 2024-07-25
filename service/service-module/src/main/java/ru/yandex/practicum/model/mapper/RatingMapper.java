package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Rating;
import ru.yandex.practicum.model.dto.RatingDto;
import ru.yandex.practicum.model.enums.RatingType;

@UtilityClass
public class RatingMapper {
    public static Rating toRating(RatingDto ratingDto) {
        if (ratingDto != null) {
            return Rating.builder()
                    .rating(RatingType.valueOf(ratingDto.getRating()))
                    .build();
        } else {
            return null;
        }
    }

    public static RatingDto toRatingDto(Rating rating) {
        if (rating != null) {
            return RatingDto.builder()
                    .id(rating.getId())
                    .rating(rating.getRating().name())
                    .build();
        } else {
            return null;
        }
    }
}
