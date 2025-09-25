package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    public Film create(Film film) {
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        return createdFilm;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        return films;
    }

    public Film getById(long id) {
        return filmStorage.getById(id);
    }

    public Film update(Film film) {
        validateFilmForUpdate(film);
        Film updateFilm = filmStorage.update(film);
        return updateFilm;
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);
        Set<Long> filmLikes = likes.computeIfAbsent(filmId, k -> new HashSet<>());
        if (filmLikes.contains(userId)) {
            throw new ru.yandex.practicum.filmorate.exception.ValidationException(
                    "Пользователь уже ставил лайк этому фильму");
        }

        filmLikes.add(userId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes != null) {
            filmLikes.remove(userId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(
                        likes.getOrDefault(f2.getId(), Collections.emptySet()).size(),
                        likes.getOrDefault(f1.getId(), Collections.emptySet()).size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
    }

    private void validateFilmForUpdate(Film film) {

        if (!filmStorage.exists(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        validateFilm(film);
    }

}