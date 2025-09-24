package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long generatedID = 0;

    @Override
    public Film create(Film film) {
        film.setId(++generatedID);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == 0 || !films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());

    }

    @Override
    public Film getById(long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    @Override
    public void delete(long id) {
        if (!films.containsKey(id)) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Фильм с id " + id + " не найден");
        }
        films.remove(id);
    }

    @Override
    public boolean exists(long id) {
        return films.containsKey(id);
    }

}