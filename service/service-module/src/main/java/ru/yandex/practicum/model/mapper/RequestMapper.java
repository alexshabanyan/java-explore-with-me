package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.model.dto.EventRequestStatusUpdateResultDto;
import ru.yandex.practicum.model.dto.ParticipationRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        if (request != null) {
            return ParticipationRequestDto.builder()
                    .created(request.getCreated())
                    .event(request.getEvent().getId())
                    .id(request.getId())
                    .requester(request.getRequester().getId())
                    .status(request.getStatus().name())
                    .build();
        } else {
            return null;
        }
    }

    public static EventRequestStatusUpdateResultDto toEventRequestStatusUpdateResultDto(
            List<Request> confirmedRequests, List<Request> rejectedRequests) {
        return EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmedRequests.stream()
                        .map(RequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toList()))
                .rejectedRequests(rejectedRequests.stream()
                        .map(RequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
