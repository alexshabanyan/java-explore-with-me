package ru.yandex.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.ConflictException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.dto.UserDto;
import ru.yandex.practicum.model.enums.RatingSortType;
import ru.yandex.practicum.model.mapper.UserMapper;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setRating(0L);
        User userDb = userRepository.save(user);
        checkEmail(userDb.getId(), userDb.getEmail());
        log.info("Пользователь добавлен в базу данных в таблицу users по ID: {} \n {}", userDb.getId(), userDb);
        return UserMapper.toUserDto(userDb);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers(List<Long> ids, Pageable pageable) {
        List<UserDto> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll(pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
        log.info("Получено {} пользователей из базы данных из таблицы users.", users.size());
        return users;
    }

    @Transactional
    @Override
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID: " + userId + " не найден."));
        userRepository.deleteById(userId);
        log.info("Пользователь удален из базы данных из таблицы users по ID: {} \n {}", userId, user);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsersSortedByRating(RatingSortType sort, Pageable pageable) {
        List<User> users = new ArrayList<>();
        switch (sort) {
            case ASC:
                users = userRepository.findAll(pageable).stream()
                        .sorted(Comparator.comparing(User::getRating).thenComparing(User::getId))
                        .collect(Collectors.toList());
                break;
            case DESC:
                users = userRepository.findAll(pageable).stream()
                        .sorted(Comparator.comparing(User::getRating).reversed().thenComparing(User::getId))
                        .collect(Collectors.toList());
                break;
        }

        List<UserDto> usersRes = users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());

        log.info("Получено {} пользователей из базы данных из таблицы users.", usersRes.size());
        return usersRes;
    }

    private void checkEmail(Long userId, String email) {
        List<User> usersSameEmail = userRepository.findAllByEmail(email).stream()
                .filter(u -> !Objects.equals(u.getId(), userId))
                .collect(Collectors.toList());

        if (!usersSameEmail.isEmpty()) {
            log.info("Email {} пользователя ID: {} уже есть у пользователей: {}.",
                    email, userId, usersSameEmail);
            throw new ConflictException("Email пользователя не может повторяться.");
        }
    }
}
