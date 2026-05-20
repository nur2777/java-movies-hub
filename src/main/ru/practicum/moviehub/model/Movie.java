package ru.practicum.moviehub.model;

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
        this.title = title;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
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