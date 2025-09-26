package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUsers(User user) {
        validateUser(user);
        User createUsers = userStorage.createUser(user);
        log.info("Добавлен новый пользователь: {}", user);
        return createUsers;
    }

    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users;
    }

    public User getById(long id) {
        User user = userStorage.getById(id);
        log.info("Найден пользователь: {}", user);
        return user;
    }

    public User updateUsers(User user) {
        if (!userStorage.exists(user.getId())) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateUser(user);
        User updateUsers = userStorage.updateUser(user);
        log.info("Обновлен пользователь: {}", user);
        return updateUsers;
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователь уже добавлен в друзья");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.getById(userId);
        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getById(userId);
        User otherUser = userStorage.getById(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherFriends = otherUser.getFriends();

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }
}