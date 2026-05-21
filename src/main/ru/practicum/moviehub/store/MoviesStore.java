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
     */
    public void addMovie(String title, int year) {
        lastId += 1;
        Movie movie = new Movie(lastId, title, year);
        movieList.put(lastId, movie);
    }

    /**
     * Метод добавления фильма c возвратом идентификатора добавленного фильма
     */
    public Integer addMovieReturnId(String title, int year) {
        addMovie(title, year);
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
     * Метод проверки существования фильма по идентификатору
     *
     * @param id идентификатор фильма
     * @return true если фильм существует, иначе false
     */
    public boolean filmExistsById(int id) {
        return movieList.containsKey(id);
    }

    /**
     * Метод получения фильма по идентификатору
     *
     * @param id идентификатор фильма
     * @return сущность фильма
     */
    public Movie getMovieById(int id) {
        return movieList.get(id);
    }

    /**
     * Метод получения списка фильма по заданному году
     *
     * @param year год выхода фильма
     * @return список фильмовы
     */
    public List<Movie> getMovieByYear(int year) {
        return movieList.values()
                .stream()
                .filter(movie -> movie.getYear() == year)
                .toList();
    }

    /**
     * Очищает полностью список фильмов пользователя
     */
    public void clearStore() {
        movieList.clear();
        this.lastId = 0;
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