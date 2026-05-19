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
import static ru.practicum.moviehub.MovieHubApp.CT_JSON;

public class GetMoviesWithYearApiTest {

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
    void getMoviesYear_whenFilmsExist_returnsNotEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies?year=1999"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица",1999);
        moviesStore.addMovie("Зеленая миля",1999);
        moviesStore.addMovie("Бригада",2002);
        moviesStore.addMovie("Интерстеллар",2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        MoviesStore sourceMovieStore = new MoviesStore();
        sourceMovieStore.addMovie("Матрица",1999);
        sourceMovieStore.addMovie("Зеленая миля",1999);
        String sourceMoviesJson = MovieHubApp.gson.toJson(sourceMovieStore.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson,body,"Исходный JSON и JSON-массив тела ответа не совпадают");
    }

    @Test
    void getMoviesYear_whenFilmNotFound_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies?year=2099"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица",1999);
        moviesStore.addMovie("Бригада",2002);
        moviesStore.addMovie("Интерстеллар",2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.OK.getCode(), resp.statusCode(), "GET /movies должен вернуть 200");
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(new MoviesStore().getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson,body,"Ожидается пустой JSON-массив");
    }
}