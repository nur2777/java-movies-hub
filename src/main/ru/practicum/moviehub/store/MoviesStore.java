package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.List;

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

    /**
     * Метод поиска фильма по идентификатору
     *
     * @param id идентфикатор фильма
     * @return сущность фильма
     */
    public Movie getMovieById(int id) {
        return movieList.get(id);
    }

    /**
     * Очищает полностью список фильмов пользователя
     */
    public void clearStore() {
        movieList.clear();
    }

    /**
     * Метод возвращает все фильмы списка
     *
     * @return список фильмов
     */
    public List<Movie> getAllMovies() {
        return movieList.values().stream().toList();
    }

}