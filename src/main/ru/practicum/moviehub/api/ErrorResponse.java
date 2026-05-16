package ru.practicum.moviehub.api;

import java.util.ArrayList;

/**
 * Класс описывает сущность ошибки запроса
 */
public class ErrorResponse {
    /**
     * Короткое описание ошибки
     */
    private String error;
    /**
     * Массив строк с деталями проблемы
     */
    private ArrayList<String> details;
}