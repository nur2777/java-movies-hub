package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.MovieHubApp;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.moviehub.MovieHubApp.CT_JSON;

public class GetMoviesByIdApiTest {

    public static final int duration = 2;
    private static MoviesServer server;
    private static HttpClient client;
    private static final MoviesStore moviesStore = new MoviesStore();

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(moviesStore, MovieHubApp.PORT);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(duration)).build();
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        moviesStore.clearStore();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMoviesId_whenFilmExist_returnsFilm() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies/1"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица", 1999);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(moviesStore.getMovieById(1));
        assertEquals(sourceMoviesJson, body, "Исходный JSON фильма и JSON тела ответа не совпадают");
    }

    @Test
    void getMoviesId_whenFilmNotFound_returnsError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies/11"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица", 1999);
        moviesStore.addMovie("Бригада", 2002);
        moviesStore.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Not_found.getCode(), resp.statusCode(), "GET /movies должен вернуть 404");
        String body = resp.body().trim();
        ArrayList<String> details = new ArrayList<>(List.of("Фильм не найден"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при получении фильма", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson, body, "Ожидается JSON c детальным описанием - Фильм не найден");
    }

    @Test
    void getMoviesId_whenIdIncorrect_returnsError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies/11ee22"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица", 1999);
        moviesStore.addMovie("Бригада", 2002);
        moviesStore.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Bad_Request.getCode(), resp.statusCode(), "GET /movies должен вернуть 400");
        String body = resp.body().trim();
        ArrayList<String> details = new ArrayList<>(List.of("Некорректный ID"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при получении фильма", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson, body, "Ожидается JSON c детальным описанием - Некорректный ID");
    }
}