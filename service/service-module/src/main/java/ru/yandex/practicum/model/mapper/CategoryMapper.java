package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.dto.CategoryDto;

@UtilityClass
public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        if (categoryDto != null) {
            return Category.builder()
                    .id(categoryDto.getId())
                    .name(categoryDto.getName())
                    .build();
        } else {
            return null;
        }
    }

    public static CategoryDto toCategoryDto(Category category) {
        if (category != null) {
            return CategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
        } else {
            return null;
        }
    }
}
