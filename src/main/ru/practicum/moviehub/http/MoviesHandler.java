package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Класс хендлера для запроса на эндпоинт /movies
 */
public class MoviesHandler extends BaseHttpHandler {

    /**
     * Переопределенный метод хендлера
     *
     * @param ex the exchange containing the request from the
     *           client and used to send the response
     */
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        switch (method.toUpperCase()) {
            case "GET":
                super.sendJson(ex, HttpStatusCodes.OK.getCode(), "[]");
            default:
                super.sendNoContent(ex, HttpStatusCodes.Bad_Request.getCode());
        }
    }
}
