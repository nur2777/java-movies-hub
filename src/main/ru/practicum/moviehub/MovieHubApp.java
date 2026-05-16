package ru.practicum.moviehub;

import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.store.MoviesStore;

public class MovieHubApp {
    /**
     * Порт приложения
     */
    public static final int PORT = 8080;
    /**
     * Константа базовой части пути адреса запроса
     */
    public static final String BaseURL = "http://localhost:";

    public static void main(String[] args) {
        final MoviesServer server = new MoviesServer(new MoviesStore(), PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
        System.out.println("Добро пожаловать в сервис MovieHub!");
    }
}