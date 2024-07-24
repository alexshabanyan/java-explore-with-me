package ru.yandex.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.model.Compilation;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.dto.CompilationDto;
import ru.yandex.practicum.model.dto.NewCompilationDto;
import ru.yandex.practicum.model.dto.UpdateCompilationRequestDto;
import ru.yandex.practicum.model.mapper.CompilationMapper;
import ru.yandex.practicum.repository.CompilationRepository;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.service.CompilationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        List<Event> events = null;

        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(compilationDto.getEvents());
        }

        Compilation compilationDb = compilationRepository.save(CompilationMapper.toCompilation(compilationDto, events));
        log.info("Подборка добавлена в базу данных в таблицу compilations по ID: {} \n {}", compilationDb.getId(), compilationDb);
        return CompilationMapper.toCompilationDto(compilationDb);
    }

    @Transactional
    @Override
    public void deleteCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка по ID: " + compId + " не найдена."));
        compilationRepository.deleteById(compId);
        log.info("Подборка удалена из базы данных из таблицы compilations по ID: {} \n {}", compId, compilation);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto compilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка по ID: " + compId + " не найдена."));

        if (compilationDto.getPinned() != null && compilationDto.getPinned() != compilation.getPinned()) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null && !compilationDto.getTitle().equals(compilation.getTitle())) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (isEventsChanged(compilation, compilationDto)) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            compilation.setEvents(events);
        }

        Compilation compilationUpd = compilationRepository.saveAndFlush(compilation);
        log.info("Подборка обновлена в базе данных в таблице compilations по ID: {} \n {}", compId, compilationUpd);
        return CompilationMapper.toCompilationDto(compilationUpd);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Pageable pageable) {
        List<CompilationDto> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        } else {
            compilations = compilationRepository.findAll(pageable).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        log.info("Получено {} подборок из базы данных из таблицы compilations.", compilations.size());
        return compilations;
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка по ID: " + compId + " не найдена."));
        log.info("Подборка по ID: {} получена из базы данных: {}.", compId, compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    private boolean isEventsChanged(Compilation compilation,
                                    UpdateCompilationRequestDto updateCompilationRequestDto) {
        return updateCompilationRequestDto.getEvents() != null &&
                !updateCompilationRequestDto.getEvents().stream().sorted()
                        .equals(compilation.getEvents().stream()
                                .map(Event::getId)
                                .sorted()
                                .collect(Collectors.toList()));
    }
}
