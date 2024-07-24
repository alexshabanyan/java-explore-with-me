package ru.yandex.practicum.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.model.enums.EventStateAction;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
public class UpdateEventAdminRequestDto {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Length(min = 20, max = 2000)
    private String annotation;

    @Positive
    private Long category;

    @Length(min = 20, max = 7000)
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

    @Length(min = 3, max = 120)
    private String title;

}
