package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.HitDto;
import ru.yandex.practicum.dto.StatsDto;
import ru.yandex.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsServerController {
    private final StatsService statsService;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    public ResponseEntity<HitDto> addHit(@Valid @RequestBody HitDto hitDto) {
        log.info("Получен POST запрос на добавление нового хита: {}", hitDto);
        statsService.saveHit(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime end,
                                   @RequestParam(required = false, name = "uris") List<String> uris,
                                   @RequestParam(required = false, name = "unique", defaultValue = "false") Boolean isUnique) {
        log.info("Получен GET запрос на получение статистики от даты: {}, до даты: {}. " +
                "Параметры поиска: список uri: {}, учет уникальных посещений: {}.", start, end, uris, isUnique);
        return statsService.getStats(start, end, uris, isUnique);
    }
}
