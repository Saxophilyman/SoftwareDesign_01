//Приёмы понятны, когда они кем-то написаны))
//В своё время делал очень простой генератор римских имён, но если отталкиваться, что нужны не только римские имена, а варианты "имён"
enum Culture { ROMAN, GREEK, GERMANIC }
enum Gender { MALE, FEMALE }

class NameContext {
    private final Culture culture;
    private final Gender gender;

    public NameContext(Culture culture, Gender gender) {
        this.culture = culture;
        this.gender = gender;
    }

    public Culture getCulture() { return culture; }
    public Gender getGender() { return gender; }
}
//итоговое сгенерировнное имя
class GeneratedName {
    private final String full;

    public GeneratedName(String full) {
        this.full = full;
    }

    public String getFull() { return full; }
}

//интерфейс
interface NameGenerator {
    Culture supportedCulture();
    GeneratedName generate(NameContext context);
}

//условно класс для конкретной генерации в зависимости от условий
class RomanNameGenerator implements NameGenerator {
    //связка с enum по культуре
    @Override
    public Culture supportedCulture() {
        return Culture.ROMAN;
    }

    @Override
    public GeneratedName generate(NameContext context) {
        //логика генерации
        String fullName = "Gaius Julius Caesar"; // вощможный результат
        return new GeneratedName(fullName);
    }
}


//Класс, использующий интерфейс
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class NameService {
    private final Map<Culture, NameGenerator> generators = new EnumMap<>(Culture.class);

    public NameService(List<NameGenerator> generatorList) {
        for (NameGenerator g : generatorList) {
            generators.put(g.supportedCulture(), g);
        }
    }

    public GeneratedName generateName(NameContext context) {
        NameGenerator generator = generators.get(context.getCulture());
        if (generator == null) {
            throw new IllegalArgumentException("No generator for culture: " + context.getCulture());
        }
        return generator.generate(context);
    }
}

class Demo {
    public static void main(String[] args) {
        NameService service = new NameService(
                List.of(new RomanNameGenerator() /*, new GreekNameGenerator(), ... */)
        );

        NameContext ctx = new NameContext(Culture.ROMAN, Gender.MALE);
        GeneratedName name = service.generateName(ctx);

        System.out.println(name.getFull());
    }
}
