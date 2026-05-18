package ru.practicum.moviehub.http;

import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.MovieHubApp;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.moviehub.MovieHubApp.CT_JSON;

public class MoviesApiTest {

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
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        getMovies_commonTests(resp);
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(moviesStore.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson,body,"Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_whenNotEmpty_returnsNotEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .GET()
                .build();
        moviesStore.addMovie("Матрица",1999);
        moviesStore.addMovie("Бригада",2002);
        moviesStore.addMovie("Интерстеллар",2014);
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        getMovies_commonTests(resp);
        String body = resp.body().trim();
        String sourceMoviesJson = MovieHubApp.gson.toJson(moviesStore.getAllMovies().stream().toList());
        assertEquals(sourceMoviesJson,body,"Исходный JSON и JSON-массив тела ответа не совпадают");
    }

    @Test
    void postMovies_withCorrectBody_returnsBodyWithId() throws Exception {
        String requestBody = "{\"title\":\"Матрица\", \"year\":1999}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers("Content-Type","application/json")
                .headers("Content-Type","charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Created.getCode(), resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();

        Movie testMovie = new Movie(1,"Матрица",1999);
        String testMovieJson = MovieHubApp.gson.toJson(testMovie);
        assertEquals(testMovieJson,body,"Ожидается JSON созданного фильма с присвоенным ID");
    }

    @Test
    void postMovies_withIncorrectJson_returnsError() throws Exception {
        String incorrectRequestBody = "{\"test\":\"test\", \"error\":1, \"description\":\"test description\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(incorrectRequestBody))
                .headers("Content-Type","application/json")
                .headers("Content-Type","charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Unprocessable_Entity.getCode(), resp.statusCode(), "POST /movies должен " +
                "вернуть 422");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();

        ArrayList<String> details = new ArrayList<>(List.of("Тело запроса содержит некорректный JSON"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson,body,"Ожидается JSON c детальным описанием - некорректный JSON");
    }

    @Test
    void postMovies_withIncorrectContentType_returnsError() throws Exception {
        String requestBody = "{\"title\":\"Матрица\", \"year\":1999}";
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .setHeader("Content-Type","text/html")
                .build();

        HttpResponse<String> resp = client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Unsupported_Media_Type.getCode(), resp.statusCode(), "POST /movies " +
                "должен вернуть 415");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать application/json " +
                "и кодировку charset=UTF-8");

        String body = resp.body().trim();

        ArrayList<String> details = new ArrayList<>(List.of("Неверный заголовок Content-Type"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson,body,"Ожидается JSON c детальным описанием - некорректный JSON");
    }

    @Test
    void postMovies_withEmptyTitle_returnsError() throws Exception {
        String requestBody = "{\"title\":null, \"year\":1999}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MovieHubApp.BaseURL + MovieHubApp.PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .headers("Content-Type","application/json")
                .headers("Content-Type","charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(HttpStatusCodes.Unprocessable_Entity.getCode(), resp.statusCode(), "POST /movies " +
                "должен вернуть 422");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue, "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();

        ArrayList<String> details = new ArrayList<>(List.of("JSON содержит пустой title"));
        ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации", details);
        String testErrorJson = MovieHubApp.gson.toJson(errorResponse);
        assertEquals(testErrorJson,body,"Ожидается JSON c детальным описанием - содержит пустой title");
    }

}