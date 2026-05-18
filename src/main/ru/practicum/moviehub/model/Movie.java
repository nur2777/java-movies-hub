package ru.practicum.moviehub.model;

import java.time.LocalDate;

/**
 * Класс для реализации сущности Фильм
 */
public class Movie {
    /**
     * Идентификатор фильма
     */
    private int id;
    /**
     * Название фильма, длина ≤ 100 символов.
     */
    private String title;
    /**
     * Год выхода фильма
     */
    private int year;

    public Movie(int id, String title, int year) {
        this.id = id;
        setTitle(title);
        setYear(year);
    }

    public Movie(String title, int year) {
        setTitle(title);
        setYear(year);
    }

    public String getTitle() {
        return title;
    }

    /**
     * Присвоение названия фильма, длина ≤ 100 символов.
     *
     * @param title название фильма
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    /**
     * Присвоение года фильма
     * число от 1888 (год самого раннего из сохранившихся фильмов) до текущий год + 1.
     *
     * @param year название фильма
     */
    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", year=" + year +
                '}';
    }
}