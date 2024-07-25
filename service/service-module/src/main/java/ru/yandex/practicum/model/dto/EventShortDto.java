package ru.yandex.practicum.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventShortDto {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Long id;

    private String annotation;

    private CategoryDto category;

    private Integer confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    private LocalDateTime eventDate;

    private UserDto initiator;

    private Boolean paid;

    private String title;

    private Long views;

    private Long rating;
}
