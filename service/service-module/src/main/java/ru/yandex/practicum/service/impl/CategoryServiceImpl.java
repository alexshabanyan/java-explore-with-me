package ru.yandex.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.ConflictException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.dto.CategoryDto;
import ru.yandex.practicum.model.mapper.CategoryMapper;
import ru.yandex.practicum.repository.CategoryRepository;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.service.CategoryService;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        Category categoryDb = categoryRepository.save(category);
        checkName(categoryDb.getId(), categoryDb.getName());
        log.info("Категория добавлена в базу данных в таблицу categories по ID: {} \n {}", categoryDb.getId(), categoryDb);
        return CategoryMapper.toCategoryDto(categoryDb);
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория по ID: " + catId + " не найдена."));
        List<Event> eventsWithCat = eventRepository.findAllByCategoryId(catId);
        if (!eventsWithCat.isEmpty()) {
            throw new ConflictException("Категорию " + catId + " нельзя удалить, тк к ней привязаны события: "
                    + eventsWithCat.stream().map(Event::getId).collect(Collectors.toList()));
        }
        categoryRepository.deleteById(catId);
        log.info("Категория удалена из базы данных из таблицы categories по ID: {} \n {}", catId, category);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория по ID: " + catId + " не найдена."));
        log.info("Категория по ID: {} получена из базы данных: {}.", catId, category);
        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category categoryOld = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория по ID: " + catId + " не найдена."));

        checkName(catId, categoryDto.getName());
        categoryDto.setId(catId);

        Category categoryUpd = categoryRepository.save(CategoryMapper.toCategory(categoryDto));
        log.info("Категория обновлена в базе данных в таблице categories по ID: {} \n {}", catId, categoryUpd);

        return CategoryMapper.toCategoryDto(categoryUpd);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getAllCategories(Pageable pageable) {
        List<CategoryDto> categories = categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
        log.info("Получено {} категорий из базы данных из таблицы categories.", categories.size());
        return categories;
    }

    private void checkName(Long catId, String name) {
        List<Category> categoriesSameName = categoryRepository.findAllByName(name).stream()
                .filter(cat -> !Objects.equals(cat.getId(), catId))
                .collect(Collectors.toList());

        if (!categoriesSameName.isEmpty()) {
            log.info("Имя {} категогии ID: {} уже есть у категории: {}.",
                    name, catId, categoriesSameName);
            throw new ConflictException("Имя категории не может повторяться.");
        }
    }
}
