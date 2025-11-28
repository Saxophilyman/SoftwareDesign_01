//Было немало абстракционных примеров, попробую довести вариант с JSON
//Главный вопрос правильно ли я делаю

//общий интерфейс, где ммы уходим от jackson, концентрируясь на типе билиотек, работающих с json
public interface JsonService {
    <T> T fromJson(String json, Class<T> type);
    String toJson(Object value);
}

//вариант использования библиотеки jackson
public class JacksonJsonService implements JsonService {

    private final ObjectMapper mapper;

    public JacksonJsonService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга JSON", e);
        }
    }

    @Override
    public String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации JSON", e);
        }
    }
}

//вариант с gson
public class GsonJsonService implements JsonService {

    private final Gson gson;

    public GsonJsonService(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    @Override
    public String toJson(Object value) {
        return gson.toJson(value);
    }
}

//выводим работу с json в отдельный сервис. Нам не важно будет какая будет использована реализация библиотеки
public class NotificationService {

    private final JsonService jsonService;

    public NotificationService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    public void sendUserInfo(UserDto user) {
        String body = jsonService.toJson(user);
        // возможная логика
        System.out.println("Sending: " + body);
    }
}

public class Main {
    public static void main(String[] args) {
        //Пока определяем работу с библиотекой jackson, но можно и gson сделать
        JsonService jsonService = new JacksonJsonService(new ObjectMapper());
        NotificationService notificationService = new NotificationService(jsonService);

        UserDto user = UserDto.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan.petrov@example.com")
                .build();

        notificationService.sendUserInfo(user);

        String json = jsonService.toJson(user);
        System.out.println("JSON: " + json);

        UserDto parsed = jsonService.fromJson(json, UserDto.class);
        System.out.println("Parsed back: " + parsed);
    }
}
