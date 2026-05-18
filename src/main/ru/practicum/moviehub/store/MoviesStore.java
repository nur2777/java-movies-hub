package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.time.LocalDate;
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
    public void addMovie(String title,int year) {
        lastId += 1;
        Movie movie = new Movie(lastId,title,year);
        movieList.put(lastId, movie);
    }

    /**
     * Метод добавления фильма c возвратом идентификатора добавленного фильма
     */
    public Integer addMovieReturnId(String title,int year) {
        addMovie(title,year);
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