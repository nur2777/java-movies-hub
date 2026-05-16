package ru.practicum.moviehub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    /**
     * Константа стандартного содержимого заголовка Content-Type
     */
    public static final String CT_JSON = "application/json; charset=UTF-8";

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