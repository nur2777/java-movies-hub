package ru.practicum.moviehub.model;

import java.time.LocalDate;

/**
 * Класс для реализации сущности Фильм
 */
public class Movie {
    /**
     * Минимально возможный год согласно ТЗ
     * от 1888 (год самого раннего из сохранившихся фильмов)
     */
    public static final int minYear = 1888;
    /**
     * Максимально возможный год согласно ТЗ
     * до текущий год + 1
     */
    public static final int maxYear = LocalDate.now().getYear() + 1;
    /**
     * Максимальная длинна названия фильма
     */
    public static final int maxTitleLength = 100;

    /**
     * Название фильма, длина ≤ 100 символов.
     */
    private String title;
    /**
     * Год выхода фильма
     */
    private int year;

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
        if (title.length() > maxTitleLength) {
            throw new IllegalArgumentException("Длинна названия фильма должна быть равна или меньше " + maxTitleLength
                    + " символов. Текущая длинна : " + title.length());
        }
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
        if (year < minYear || year >= maxYear) {
            throw new IllegalArgumentException("Год выхода фильма должна быть от " + minYear + " до " + maxYear +
                    "Указанный год : " + year);
        }
        this.year = year;
    }
}