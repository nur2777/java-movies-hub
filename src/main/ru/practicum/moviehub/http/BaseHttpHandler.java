package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Базовый класс для всех хендлеров приложения
 */
public abstract class BaseHttpHandler implements HttpHandler {
    /**
     * Константа для случая отсутствия ответа
     */
    private static final int RESPONSE_LENGTH = -1;
    /**
     * Константа стандартного содержимого заголовка Content-Type
     */
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    /**
     * Базовая реализация отправки ответа с содержимым
     *
     * @param ex     данные запроса
     * @param status статус ответа
     * @param json   тело ответа в формате JSON
     */
    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, json.length());
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Общий для всех хендлеров метод для отправки ответа без тела и кодом "Нет содержимого"
     *
     * @param ex данные запроса
     */
    protected void sendNoContent(HttpExchange ex, int status) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, RESPONSE_LENGTH);
    }
}