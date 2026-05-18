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

    public ErrorResponse(String error) {
        this.error = error;
        this.details = new ArrayList<>();
    }

    public ErrorResponse(String error, ArrayList<String> details) {
        this.error = error;
        this.details = details;
    }

    public void addNewDetails(String detail) {
        System.out.println("Новое описание ошибки:" + detail);
        details.add(detail);
    }
}