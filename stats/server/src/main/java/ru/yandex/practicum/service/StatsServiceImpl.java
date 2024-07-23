package ru.yandex.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.HitDto;
import ru.yandex.practicum.dto.StatsDto;
import ru.yandex.practicum.model.Hit;
import ru.yandex.practicum.model.HitMapper;
import ru.yandex.practicum.repository.HitRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final HitRepository repository;

    @Transactional
    @Override
    public void saveHit(HitDto hitDto) {
        Hit hitDb = repository.save(HitMapper.toHit(hitDto));
        log.info("Хит {} был сохранен в ДБ в таблицу hits по ID: {}.", hitDb, hitDb.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean isUnique) {
        if (start.isEqual(end) || start.isAfter(end)) {
            throw new ValidationException("Дата начала (" + start + ") должна быть строго раньше даты конца (" + end +
                    ").");
        }

        List<StatsDto> stats;

        if (uris != null && !uris.isEmpty()) {
            if (isUnique) {
                stats = repository.findUniqueHitsByUris(start, end, uris);
            } else {
                stats = repository.findHitsByUris(start, end, uris);
            }
        } else {
            if (isUnique) {
                stats = repository.findUniqueHits(start, end);
            } else {
                stats = repository.findHits(start, end);
            }
        }

        log.info("Получена следующая статистика: {}", stats.stream()
                .map(s -> "StatsDto{" +
                        "app='" + s.getApp() + '\'' +
                        ", uri='" + s.getUri() + '\'' +
                        ", hits='" + s.getHits() + '\'' +
                        '}')
                .collect(Collectors.toList()));
        return stats;
    }
}
