package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;

/**
 * Класс реализует хранение списка фильмов пользователя
 */
public class MoviesStore {
    /**
     * Список фильмов
     */
    private HashMap<Integer, Movie> movieList;
    /**
     * Последний идентификатор
     */
    private int lastId = 0;

    public MoviesStore() {
        this.movieList = new HashMap<Integer, Movie>();
    }

    /**
     * Метод добавления фильма
     *
     * @return идентификатор фильма
     */
    public Integer addMovie(Movie movie) {
        lastId += 1;
        movieList.put(lastId, movie);
        return lastId;
    }

    /**
     * Метод удаления фильма
     *
     * @param id идентификатор фильма
     */
    public void deleteMovie(Integer id) {
        movieList.remove(id);
    }
}