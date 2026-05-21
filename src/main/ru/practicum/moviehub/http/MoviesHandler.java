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
    /**
     * Основное хранилище фильмов пользователя
     */
    private MoviesStore moviesStore;
    /**
     * Минимально возможный год согласно ТЗ
     * от 1888 (год самого раннего из сохранившихся фильмов)
     */
    private static final int MIN_YEAR = 1888;
    /**
     * Максимально возможный год согласно ТЗ
     * до текущий год + 1
     */
    private static final int MAX_YEAR = LocalDate.now().getYear() + 1;
    /**
     * Максимальная длинна названия фильма
     */
    private static final int MAX_TITLE_LENGTH = 100;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    /**
     * Переопределенный метод хендлера
     *
     * @param ex данные запроса
     */
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String[] splitStrings = path.split("/");
        String queryParams = ex.getRequestURI().getQuery();
        if (splitStrings.length > 1 && splitStrings[1].equals("movies")) {
            switch (method.toUpperCase()) {
                case "GET":
                case "DELETE":
                    getDeleteMovies(ex, splitStrings, method.toUpperCase(), queryParams);
                    break;
                case "POST":
                    postMovies(ex);
                    break;
                default:
                    super.sendNoContent(ex, HttpStatusCodes.Method_Not_Allowed.getCode());
            }
        } else {
            super.sendNoContent(ex, HttpStatusCodes.Bad_Request.getCode());
        }
    }


    /** Метод обработки запросов GET и DELETE /movies
     * @param ex данные запроса
     * @param splitStrings массив элементов пути запроса
     * @param method метод запроса
     * @param queryParams параметры строки запроса
     */
    private void getDeleteMovies(HttpExchange ex, String[] splitStrings, String method, String queryParams) throws IOException {
        String responseJson = "";
        HttpStatusCodes httpStatusCode = HttpStatusCodes.OK;
        ArrayList<String> errorDetails = new ArrayList<>();
        if (splitStrings.length == 2 && method.equals("GET")) {
            if (queryParams == null) {
                responseJson = MovieHubApp.gson.toJson(moviesStore.getAllMovies());
            } else {
                try {
                    int idx = queryParams.indexOf("=");
                    int year = Integer.parseInt(queryParams.substring(idx + 1));
                    responseJson = MovieHubApp.gson.toJson(moviesStore.getMovieByYear(year));
                } catch (NumberFormatException e) {
                    httpStatusCode = HttpStatusCodes.Bad_Request;
                    errorDetails.add("Некорректный параметр запроса — 'year'");
                }
            }
        } else if ((splitStrings.length == 3) && !splitStrings[2].isEmpty()) {
            try {
                int id = Integer.parseInt(splitStrings[2]);
                if (moviesStore.filmExistsById(id)) {
                    if (method.equals("GET")) {
                        responseJson = MovieHubApp.gson.toJson(moviesStore.getMovieById(id));
                    } else if (method.equals("DELETE")) {
                        moviesStore.deleteMovie(id);
                        httpStatusCode = HttpStatusCodes.No_content;
                    }
                } else {
                    httpStatusCode = HttpStatusCodes.Not_found;
                    errorDetails.add("Фильм не найден");
                }
            } catch (NumberFormatException e) {
                httpStatusCode = HttpStatusCodes.Bad_Request;
                errorDetails.add("Некорректный ID");
            }
        } else {
            httpStatusCode = HttpStatusCodes.Bad_Request;
            errorDetails.add("Некорректный путь запроса");
        }

        if (!errorDetails.isEmpty()) {
            ErrorResponse errorResponse;
            if (method.equals("GET")) {
                errorResponse = new ErrorResponse("Ошибка при получении фильма", errorDetails);
            } else {
                errorResponse = new ErrorResponse("Ошибка при удалении фильма", errorDetails);
            }
            responseJson = MovieHubApp.gson.toJson(errorResponse);
        }
        if (httpStatusCode == HttpStatusCodes.No_content) {
            super.sendNoContent(ex, httpStatusCode.getCode());
        } else {
            super.sendJson(ex, httpStatusCode.getCode(), responseJson);
        }
    }

    /**
     * Метод обработки запроса POST /movies
     *
     * @param ex данные запроса
     */
    private void postMovies(HttpExchange ex) throws IOException {
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
                    if (title.length() > MAX_TITLE_LENGTH) {
                        errorDetails.add("Длинна названия фильма должна быть меньше или равна " + MAX_TITLE_LENGTH +
                                " символов.");
                    }
                }
                // получаем год выхода
                JsonElement yearElement = jsonObject.get("year");
                if (yearElement != null && yearElement.isJsonNull()) {
                    errorDetails.add("JSON содержит пустой year");
                } else {
                    int year = jsonObject.get("year").getAsInt();
                    if (year < MIN_YEAR || year >= MAX_YEAR) {
                        errorDetails.add("Год выхода фильма должна быть от " + MIN_YEAR + " до " + MAX_YEAR);
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
            ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации", errorDetails);
            responseJson = MovieHubApp.gson.toJson(errorResponse);
        }
        super.sendJson(ex, httpStatusCode.getCode(), responseJson);
    }
}
