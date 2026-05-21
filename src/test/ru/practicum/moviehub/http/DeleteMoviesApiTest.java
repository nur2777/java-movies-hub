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
import static ru.practicum.moviehub.http.GetMoviesApiTest.*;

public class DeleteMoviesApiTest {

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
    void deleteMovies_whenFilmExist_returnsFilm() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies/2"))
                .DELETE()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.No_content.getCode(), resp.statusCode(), "DELETE /movies должен вернуть 204");
        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void deleteMovies_whenFilmNotFound_returnsError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies/11"))
                .DELETE()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        MOVIES_STORE.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Not_found.getCode(), resp.statusCode(), "DELETE /movies должен вернуть 404");
        String body = resp.body().trim();
        ArrayList<String> details = new ArrayList<>(List.of("Фильм не найден"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при удалении фильма", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson, body, "Ожидается JSON c детальным описанием - Фильм не найден");
    }

    @Test
    void deleteMovies_whenIdIncorrect_returnsError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PORT + "/movies/11ee22"))
                .DELETE()
                .build();
        MOVIES_STORE.addMovie("Матрица", 1999);
        MOVIES_STORE.addMovie("Бригада", 2002);
        MOVIES_STORE.addMovie("Интерстеллар", 2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Bad_Request.getCode(), resp.statusCode(), "DELETE /movies должен вернуть 400");
        String body = resp.body().trim();
        ArrayList<String> details = new ArrayList<>(List.of("Некорректный ID"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при удалении фильма", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson, body, "Ожидается JSON c детальным описанием - Некорректный ID");
    }
}