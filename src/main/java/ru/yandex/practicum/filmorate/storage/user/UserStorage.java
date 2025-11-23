package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    List<User> getAllUsers();

    User getById(long id);

    User updateUser(User user);

    boolean exists(long id);
}