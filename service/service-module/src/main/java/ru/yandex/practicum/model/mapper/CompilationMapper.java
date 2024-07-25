package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Compilation;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.dto.CompilationDto;
import ru.yandex.practicum.model.dto.NewCompilationDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
    public static Compilation toCompilation(NewCompilationDto compilationDto, List<Event> events) {
        if (compilationDto != null) {
            return Compilation.builder()
                    .events(events != null ? events : Collections.emptyList())
                    .pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false)
                    .title(compilationDto.getTitle())
                    .build();
        } else {
            return null;
        }
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        if (compilation != null) {
            return CompilationDto.builder()
                    .id(compilation.getId())
                    .events(compilation.getEvents().stream()
                            .map(EventMapper::toEventShortDto)
                            .collect(Collectors.toList()))
                    .pinned(compilation.getPinned())
                    .title(compilation.getTitle())
                    .build();
        } else {
            return null;
        }
    }
}
