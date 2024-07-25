package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.dto.UserDto;

@UtilityClass
public class UserMapper {
    public static User toUser(UserDto userDto) {
        if (userDto != null) {
            return User.builder()
                    .id(userDto.getId())
                    .name(userDto.getName())
                    .email(userDto.getEmail())
                    .rating(userDto.getRating())
                    .build();
        } else {
            return null;
        }
    }

    public static UserDto toUserDto(User user) {
        if (user != null) {
            return UserDto.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .rating(user.getRating())
                    .build();
        } else {
            return null;
        }
    }
}
