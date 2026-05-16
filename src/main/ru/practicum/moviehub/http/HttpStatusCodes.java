package ru.practicum.moviehub.http;

public enum HttpStatusCodes {
    //Коды успеха
    OK(200, "Запрос успешно обработан, и результат возвращён в теле ответа."),
    Created(201, "На сервере были успешно созданы один или несколько новых ресурсов."),
    No_content(204, "Запрос успешно обработан и нет никаких данных для возврата. Тело ответа " +
            "проверять не нужно."),
    //Коды клиентских ошибок
    Bad_Request(400, "Сервер не понимает запрос или пытается его обработать, но не может выполнить " +
            "из-за того, что какой-то его аспект неверен."),
    Unauthorized(401, "Для выполнения запроса нужна аутентификация, но вместе с запросом не были " +
            "переданы авторизационные данные."),
    Not_found(404, "Сервер не может найти запрашиваемый ресурс.");

    /**
     * Числовой код ответа сервера
     */
    private final int code;
    /**
     * Описание ответа сервера
     */
    private final String description;

    HttpStatusCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
