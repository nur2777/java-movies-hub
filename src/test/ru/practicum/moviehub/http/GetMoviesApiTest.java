package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.MovieHubApp;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class GetMoviesApiTest {
    private static MoviesServer server;
    private static HttpClient client;
    private static final MoviesStore MOVIES_STORE = new MoviesStore();
    static final int PORT = 8080;
    /**
     * Константа стандартного содержимого заголовка Content-Type
     */
    static final String CT_JSON = "application/json; charset=UTF-8";
    /**
     * Константа базовой части пути адреса запроса
     */
    static final String BASE_URL = "http://localhost:";
    static final int DURATION = 2;

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

    void getMovies_commonTests(HttpResponse<String> resp) {
        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        getMovies_commonTests(resp);
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(MOVIES_STORE.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson, body, "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_whenNotEmpty_returnsNotEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies"))
                .GET()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        MOVIES_STORE.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        getMovies_commonTests(resp);
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(MOVIES_STORE.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson, body, "Исходный JSON и JSON-массив тела ответа не совпадают");
    }

    @Test
    void movies_whenWrongMethod_returnsError() throws Exception {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString("{\"key\":\"value\"}");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies"))
                .PUT(bodyPublisher)
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Method_Not_Allowed.getCode(), resp.statusCode(), "PUT /movies " +
                "должен вернуть 405");
    }
}