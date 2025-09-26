package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getById(long id) {
        return filmStorage.getById(id);
    }

    public Film update(Film film) {
        validateFilm(film);
        return filmStorage.update(film);
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже ставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
    }
}