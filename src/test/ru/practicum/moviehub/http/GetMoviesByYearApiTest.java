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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.http.GetMoviesApiTest.*;

public class GetMoviesByYearApiTest {
    private static MoviesServer server;
    private static HttpClient client;
    private static final MoviesStore MOVIES_STORE = new MoviesStore();

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(MOVIES_STORE, PORT);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(DURATION)).build();
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        MOVIES_STORE.clearStore();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMoviesYear_whenFilmsExist_returnsNotEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies?year=1999"))
                .GET()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Зеленая миля", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        MOVIES_STORE.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        MoviesStore sourceMovieStore = new MoviesStore();
        sourceMovieStore.addMovie("Матрица", 1999);
        sourceMovieStore.addMovie("Зеленая миля", 1999);
        String sourceMoviesJson = MovieHubApp.gson.toJson(sourceMovieStore.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson, body, "Исходный JSON и JSON-массив тела ответа не совпадают");
    }

    @Test
    void getMoviesYear_whenFilmNotFound_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies?year=2099"))
                .GET()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        MOVIES_STORE.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(new MoviesStore().getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson, body, "Ожидается пустой JSON-массив");
    }

    @Test
    void getMoviesYear_whenYearIncorrect_returnsError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies?year=20w99"))
                .GET()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        MOVIES_STORE.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Bad_Request.getCode(), resp.statusCode(), "GET /movies должен вернуть 400");
        String body = resp.body().trim();
        ArrayList<String> details = new ArrayList<>(List.of("Некорректный параметр запроса — 'year'"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при получении фильма", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson, body, "Ожидается JSON c детальным описанием - Некорректный параметр запроса — 'year'");
    }
}