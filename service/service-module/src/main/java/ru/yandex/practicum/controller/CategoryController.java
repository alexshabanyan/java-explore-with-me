package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.dto.CategoryDto;
import ru.yandex.practicum.service.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    // ---------------admin------------------
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("Получен POST запрос на добавление новой категории: {}", categoryDto);
        return categoryService.addCategory(categoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Получен DELETE запрос на удаление категории с ID: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Получен PATCH запрос на обновление категории с ID: {}. Было:\n{}\n Стало:\n {}",
                catId, categoryService.getCategoryById(catId), categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }

    // ---------------public-----------------
    @GetMapping("/categories")
    public List<CategoryDto> getAllCategories(@RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                              @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Получен GET запрос на нахождение всех категорий с параметрами from= {} & size= {}.", from, size);
        int page = from > 0 ? from / size : from;
        return categoryService.getAllCategories(PageRequest.of(page, size));
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("Получен GET запрос на нахождение категории по ID: {}.", catId);
        return categoryService.getCategoryById(catId);
    }
}
