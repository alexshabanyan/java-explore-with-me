package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.HitDto;
import ru.yandex.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean isUnique);
}
