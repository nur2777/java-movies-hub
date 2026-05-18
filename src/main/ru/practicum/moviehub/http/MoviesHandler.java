package ru.practicum.moviehub.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.MovieHubApp;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс хендлера для запроса на эндпоинт /movies
 */
public class MoviesHandler extends BaseHttpHandler {
    MoviesStore moviesStore;
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
                super.sendJson(ex, HttpStatusCodes.OK.getCode(), json);
                break;
            case "POST":
                postRequest(ex);
                break;
            default:
                super.sendNoContent(ex, HttpStatusCodes.Bad_Request.getCode());
        }
    }

    /**
     * Обработка пост запроса
     *
     * @param ex данные запроса
     */
    private void postRequest(HttpExchange ex) throws IOException {
        InputStream inputStream = ex.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        ArrayList<String> errorDetails = new ArrayList<>();
        Headers requestHeaders = ex.getRequestHeaders();
        JsonElement jsonElement = JsonParser.parseString(body);
        List<String> contentTypeValues = requestHeaders.get("Content-type");

        if ((contentTypeValues == null)
                || !(contentTypeValues.contains("application/json"))
                || !(contentTypeValues.contains("charset=UTF-8"))) {
            errorDetails.add("Неверный заголовок Content-Type");
        } else if (!jsonElement.isJsonObject()) {
            errorDetails.add("Тело запроса не является JSON-объектом");
        } else {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.keySet().size() == 2 &&
                    jsonObject.keySet().contains("title") &&
                    jsonObject.keySet().contains("year")) {

                // получаем название фильма
                JsonElement titleElement = jsonObject.get("title");
                if (titleElement != null && titleElement.isJsonNull()) {
                    errorDetails.add("JSON содержит пустой title");
                } else {
                    String title = jsonObject.get("title").getAsString();
                    if (title.length() > maxTitleLength) {
                        errorDetails.add("Длинна названия фильма должна быть меньше или равна " + maxTitleLength +
                                " символов.");
                    }
                }

                // получаем год выхода
                JsonElement yearElement = jsonObject.get("year");
                if (yearElement != null && yearElement.isJsonNull()) {
                    errorDetails.add("JSON содержит пустой year");
                } else {
                    int year = jsonObject.get("year").getAsInt();
                    if (year < minYear || year >= maxYear) {
                        errorDetails.add("Год выхода фильма должна быть от " + minYear + " до " + maxYear);
                    }
                }
            } else {
                errorDetails.add("Тело запроса содержит некорректный JSON");
            }
        }

        HttpStatusCodes httpStatusCode = null;
        String responseJson;
        if (errorDetails.isEmpty()) {
            Movie newMovie = MovieHubApp.gson.fromJson(body, Movie.class);
            int id = moviesStore.addMovieReturnId(newMovie.getTitle(), newMovie.getYear());
            responseJson = MovieHubApp.gson.toJson(moviesStore.getMovieById(id));
            httpStatusCode = HttpStatusCodes.Created;
        } else {
            if (errorDetails.contains("Неверный заголовок Content-Type")) {
                httpStatusCode = HttpStatusCodes.Unsupported_Media_Type;
            } else {
                httpStatusCode = HttpStatusCodes.Unprocessable_Entity;
            }
            ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации",errorDetails);
            responseJson = MovieHubApp.gson.toJson(errorResponse);
        }
        super.sendJson(ex, httpStatusCode.getCode(), responseJson);
    }
}
