package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Location;
import ru.yandex.practicum.model.dto.LocationDto;

@UtilityClass
public class LocationMapper {
    public static LocationDto toLocationDto(Location location) {
        if (location != null) {
            return LocationDto.builder()
                    .lat(location.getLat())
                    .lon(location.getLon())
                    .build();
        } else {
            return null;
        }
    }

    public static Location toLocation(LocationDto locationDto) {
        if (locationDto != null) {
            return Location.builder()
                    .lat(locationDto.getLat())
                    .lon(locationDto.getLon())
                    .build();
        } else {
            return null;
        }
    }
}
