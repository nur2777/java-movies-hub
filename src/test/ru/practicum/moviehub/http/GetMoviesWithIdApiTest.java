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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.MovieHubApp.CT_JSON;

public class GetMoviesWithIdApiTest {

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

    void getMoviesId_commonTests(HttpResponse<String> resp) {

        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void getMoviesId_whenFilmExist_returnsFilm() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies/1"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица",1999);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        getMoviesId_commonTests(resp);
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(moviesStore.getMovieById(1));
        assertEquals(sourceMoviesJson,body,"Исходный JSON фильма и JSON тела ответа не совпадают");
    }

    @Test
    void getMoviesId_whenNotEmpty_returnsNotEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица",1999);
        moviesStore.addMovie("Бригада",2002);
        moviesStore.addMovie("Интерстеллар",2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        getMoviesId_commonTests(resp);
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(moviesStore.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson,body,"Исходный JSON и JSON-массив тела ответа не совпадают");
    }
}