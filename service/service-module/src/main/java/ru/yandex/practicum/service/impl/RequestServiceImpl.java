package ru.yandex.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.ConflictException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.dto.EventRequestStatusUpdateRequestDto;
import ru.yandex.practicum.model.dto.EventRequestStatusUpdateResultDto;
import ru.yandex.practicum.model.dto.ParticipationRequestDto;
import ru.yandex.practicum.model.enums.EventState;
import ru.yandex.practicum.model.enums.RequestStatus;
import ru.yandex.practicum.model.mapper.RequestMapper;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.repository.RequestRepository;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.service.RequestService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getAllRequestsForEventCreatedByUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException("Событие по ID: " + eventId + " с инициатором " + userId + " не найдено.");
        }
        List<ParticipationRequestDto> requests = requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
        log.info("Получено {} запросов из базы данных из таблицы requests.", requests.size());
        return requests;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResultDto updateRequestsStatusForEventCreatedByUser(Long userId, Long eventId,
                                                                                       EventRequestStatusUpdateRequestDto eventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        List<Request> requests = requestRepository.findAllById(eventDto.getRequestIds());

        if (!event.getInitiator().equals(user)
                || event.getConfirmedRequests().equals(event.getParticipantLimit())
                || !isValidRequestStatus(requests)
                || !event.isRequestModeration()
        ) {
            throw new ConflictException("Ошибка валидации изменения статуса запроса.");
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        AtomicReference<Integer> cntLeftConfirmations = new AtomicReference<>(
                event.getParticipantLimit() - event.getConfirmedRequests());

        if (eventDto.getStatus().equals(RequestStatus.REJECTED) || cntLeftConfirmations.get().equals(0)) {
            rejectedRequests.addAll(requests);
            requests.forEach(request -> {
                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
            });

            EventRequestStatusUpdateResultDto eventRequestDto =
                    RequestMapper.toEventRequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
            log.info("Запрос на обновление статуса события {} пользователя {} получен из базы данных из таблицы requests: {}",
                    eventId, userId, eventRequestDto);
            return eventRequestDto;
        }

        requests.forEach(request -> {
            if (cntLeftConfirmations.get() > 0) {
                request.setStatus(eventDto.getStatus());
                requestRepository.save(request);

                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);

                confirmedRequests.add(request);
                cntLeftConfirmations.getAndSet(cntLeftConfirmations.get() - 1);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
                rejectedRequests.add(request);
            }

        });

        EventRequestStatusUpdateResultDto eventRequestDto =
                RequestMapper.toEventRequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
        log.info("Запрос на обновление статуса события {} пользователя {} получен из базы данных из таблицы requests: {}",
                eventId, userId, eventRequestDto);
        return eventRequestDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getAllRequestsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        List<ParticipationRequestDto> requests = requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
        log.info("Получено {} запросов из базы данных из таблицы requests.", requests.size());
        return requests;
    }

    @Transactional
    @Override
    public ParticipationRequestDto addRequestByUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + eventId + " не найдено."));

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)
                || !event.getState().equals(EventState.PUBLISHED)
                || (event.getParticipantLimit() > 0
                && event.getConfirmedRequests().equals(event.getParticipantLimit()))
                || event.getInitiator().getId().equals(userId)
        ) {
            throw new ConflictException("Ошибка валидации нового запроса.");
        }

        RequestStatus status = RequestStatus.PENDING;
        if (event.getParticipantLimit().equals(0) || !event.isRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .status(status)
                .event(event)
                .requester(user)
                .build();

        Request requestDb = requestRepository.save(request);
        log.info("Запрос добавлен в базу данных в таблицу requests по ID: {} \n {}", requestDb.getId(), requestDb);

        if (!event.isRequestModeration()) {
            Integer cntConfirmedRequests = event.getConfirmedRequests();
            event.setConfirmedRequests(cntConfirmedRequests + 1);
            eventRepository.save(event);
        }

        return RequestMapper.toParticipationRequestDto(requestDb);
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequestByIdUserId(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос по ID: " + requestId + " не найден."));

        Event event = eventRepository.findById(request.getEvent().getId())
                .orElseThrow(() -> new NotFoundException("Событие по ID: " + request.getEvent().getId() + " не найдено."));

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            Integer cntConfirmedRequests = event.getConfirmedRequests();
            event.setConfirmedRequests(cntConfirmedRequests - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);
        Request requestDb = requestRepository.save(request);

        log.info("Статус запроса обновлен в базе данных в таблице requests по ID: {} \n {}", requestId, requestDb);
        return RequestMapper.toParticipationRequestDto(requestDb);
    }

    private boolean isValidRequestStatus(List<Request> requests) {
        return requests.stream()
                .map(Request::getStatus)
                .anyMatch(it -> it.equals(RequestStatus.PENDING));
    }
}
