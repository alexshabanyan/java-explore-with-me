package ru.yandex.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.ConflictException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.dto.UserDto;
import ru.yandex.practicum.model.mapper.UserMapper;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.service.UserService;

import java.util.List;
import java.util.Objects;
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
