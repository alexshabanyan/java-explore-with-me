package ru.yandex.practicum.service;

import ru.yandex.practicum.model.dto.EventRequestStatusUpdateRequestDto;
import ru.yandex.practicum.model.dto.EventRequestStatusUpdateResultDto;
import ru.yandex.practicum.model.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getAllRequestsForEventCreatedByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateRequestsStatusForEventCreatedByUser(Long userId, Long eventId,
                                                                                EventRequestStatusUpdateRequestDto eventDto);

    List<ParticipationRequestDto> getAllRequestsByUser(Long userId);

    ParticipationRequestDto addRequestByUser(Long userId, Long eventId);

    ParticipationRequestDto cancelRequestByIdUserId(Long userId, Long requestId);
}
