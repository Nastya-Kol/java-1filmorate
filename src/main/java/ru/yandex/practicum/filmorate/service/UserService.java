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
    private final Map<Long, Set<Long>> friends = new HashMap<>();

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
        validateUserForUpdate(user);
        User updateUsers = userStorage.updateUser(user);
        log.info("Обновлен пользователь: {}", user);
        return updateUsers;
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        Set<Long> userFriends = friends.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }

        Set<Long> friendFriends = friends.get(friendId);
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    public List<User> getFriends(long userId) {
        userStorage.getById(userId); // Проверяем существование пользователя
        Set<Long> friendIds = friends.getOrDefault(userId, Collections.emptySet());
        return friendIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        userStorage.getById(userId);
        userStorage.getById(otherId);

        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

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

    private void validateUserForUpdate(User user) {
        if (!userStorage.exists(user.getId())) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        validateUser(user);
    }
}