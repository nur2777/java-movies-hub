package ru.practicum.moviehub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.store.MoviesStore;

public class MovieHubApp {
    /**
     * Порт приложения
     */
    private static final int PORT = 8080;
    /**
     * Стандартный Gson-объект для одинакового преобразования
     */
    public static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public static void main(String[] args) {
        final MoviesServer server = new MoviesServer(new MoviesStore(), PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
        System.out.println("Добро пожаловать в сервис MovieHub!");
    }
}