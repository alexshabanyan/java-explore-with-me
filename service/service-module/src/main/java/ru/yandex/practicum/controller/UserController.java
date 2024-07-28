package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.dto.UserDto;
import ru.yandex.practicum.model.enums.RatingSortType;
import ru.yandex.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping
public class UserController {
    private final UserService userService;

    // ---------------admin------------------
    @GetMapping("/admin/users")
    public List<UserDto> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                     @RequestParam(defaultValue = "0") @Min(0) final Integer from,
                                     @RequestParam(defaultValue = "10") @Min(1) final Integer size) {
        log.info("Получен GET запрос на нахождение всех пользователей с ID: {} с параметрами from= {} & size= {}.",
                ids, from, size);
        int page = from > 0 ? from / size : from;
        return userService.getAllUsers(ids, PageRequest.of(page, size));
    }

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен POST запрос на добавление нового пользователя: {}", userDto);
        return userService.addUser(userDto);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable("userId") Long userId) {
        log.info("Получен DELETE запрос на удаление пользователя с ID: {}", userId);
        userService.deleteUserById(userId);
    }

    // ---------------private----------------
    @GetMapping("/users/rating")
    public List<UserDto> getAllUsersSortedByRating(
            @RequestParam(required = false, defaultValue = "DESC") RatingSortType sort,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Получен GET запрос на нахождение всех пользователей по рейтингу с параметрами: sort= {}; from= {}; size= {}.",
                sort, from, size);

        int page = from > 0 ? from / size : from;
        PageRequest pageRequest = PageRequest.of(page, size);

        return userService.getAllUsersSortedByRating(sort, pageRequest);
    }
}
