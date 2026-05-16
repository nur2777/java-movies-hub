package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.MovieHubApp;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

/**
 * Класс хендлера для запроса на эндпоинт /movies
 */
public class MoviesHandler extends BaseHttpHandler {
    MoviesStore moviesStore;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

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
                String json = MovieHubApp.gson.toJson(moviesStore.getAllMovies());
                //System.out.println(json);
                super.sendJson(ex, HttpStatusCodes.OK.getCode(), json);
            default:
                super.sendNoContent(ex, HttpStatusCodes.Bad_Request.getCode());
        }
    }
}
