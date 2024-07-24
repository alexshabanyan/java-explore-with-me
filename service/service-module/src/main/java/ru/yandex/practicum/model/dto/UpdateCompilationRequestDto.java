package ru.yandex.practicum.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCompilationRequestDto {
    private List<Long> events;
    private Boolean pinned;

    @Length(min = 1, max = 50)
    private String title;

}
