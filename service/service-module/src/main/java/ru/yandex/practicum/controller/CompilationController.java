package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.dto.CompilationDto;
import ru.yandex.practicum.model.dto.NewCompilationDto;
import ru.yandex.practicum.model.dto.UpdateCompilationRequestDto;
import ru.yandex.practicum.service.CompilationService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class CompilationController {
    private final CompilationService compilationService;

    // ---------------admin------------------
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/compilations")
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto compilationDto) {
        log.info("Получен POST запрос на добавление новой подборки: {}", compilationDto);
        return compilationService.addCompilation(compilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/compilations/{compId}")
    public void deleteCompilationById(@PathVariable Long compId) {
        log.info("Получен DELETE запрос на удаление подборки с ID: {}", compId);
        compilationService.deleteCompilationById(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequestDto compilationDto) {
        log.info("Получен PATCH запрос на обновление подборки с ID: {}. Было:\n{}\n Стало:\n {}",
                compId, compilationService.getCompilationById(compId), compilationDto);
        return compilationService.updateCompilation(compId, compilationDto);
    }

    // ---------------public-----------------
    @GetMapping("/compilations")
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                   @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Получен GET запрос на нахождение всех подборок с параметрами pinned= {}; from= {}; size= {}.",
                pinned, from, size);
        int page = from > 0 ? from / size : from;
        return compilationService.getAllCompilations(pinned, PageRequest.of(page, size));
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("Получен GET запрос на нахождение подборки по ID: {}.", compId);
        return compilationService.getCompilationById(compId);
    }
}
