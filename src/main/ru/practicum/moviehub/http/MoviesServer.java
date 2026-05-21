package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Класс реализует HTTP-сервер приложения
 */
public class MoviesServer {
    /**
     * Бэклог приложения
     */
    private static final int BACKLOG = 0;
    /**
     * HTTP-сервер
     */
    private final HttpServer server;

    public MoviesServer(MoviesStore moviesStore, int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), BACKLOG);
            server.createContext("/movies", new MoviesHandler(moviesStore));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    /**
     * Метод запускает сервер
     */
    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    /**
     * Метод останавливает сервер
     */
    public void stop() {
        server.stop(BACKLOG);
        System.out.println("Сервер остановлен");
    }
}