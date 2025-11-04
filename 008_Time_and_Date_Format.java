//Ждал вариантов решения по многопоточности, эх..)

/**
Какие есть недостатки у кода:
Сами классы SimpleDateFormat и Date являются устаревшими и в целом не удобны для использования, в связи со множеством нюансов. 
Date является простой формой отсчёта в мс от эпохи, нет удобных способов обработки данных.
SimpleDateFormat считается не потокобезопасным(хранит внутреннее состояние и меняет его при вызове). Вместо него рекоммендуется использовать DateTimeFormatter

Как указано в примере, нет ничего про часовые пояса и переход на летнее/зимнее время

Предположу, что брать и хранить само время лучше в форме Instant. А уже при необходимости переводить его во время конкретной зоны. Сразу задавать локали, определять формат, который будет валидным на входе
Отдельно имеет смысл подумать о способах обработки переходов на летнее/зимнее время.

Дополнительную сложность могут вызывать также и опциональные варианты. Приём формата в том или ином виде, те же способы обработки перехода времени

*/

//Улучшенный вариант
import java.time.*;
import java.time.format.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Locale;

public class DateExampleStrict {

    // 1) События: строго ISO-8601 с T и ОБЯЗАТЕЛЬНЫМ смещением (+hh:mm или Z)
    private static final DateTimeFormatter ISO_T_WITH_OFFSET =
            new DateTimeFormatterBuilder()
                    .appendPattern("uuuu-MM-dd'T'HH:mm:ssXXX") // пример: 2024-05-13T14:30:00+03:00 или ...Z
                    .toFormatter(Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

    // 2) Расписания: строго локальное время с T БЕЗ смещения
    private static final DateTimeFormatter LOCAL_T =
            new DateTimeFormatterBuilder()
                    .appendPattern("uuuu-MM-dd'T'HH:mm:ss") // пример: 2024-05-13T14:30:00
                    .toFormatter(Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        // Пример "события" (момент однозначен)
        String eventRaw = "2024-05-13T14:30:00+03:00";
        Instant eventInstant = parseEventInstant(eventRaw);
        System.out.println("EVENT  UTC : " + eventInstant);

        // Пример "расписания" (локальное время + зона домена)
        String scheduleRaw = "2024-05-13T14:30:00";
        ZoneId domainZone = ZoneId.of("Europe/Tallinn");
        Instant scheduleInstant = parseScheduleLocal(scheduleRaw, domainZone);
        System.out.println("SCHED  UTC : " + scheduleInstant);
        System.out.println("SCHED as " + domainZone + " : " + scheduleInstant.atZone(domainZone));
    }

    // Парсит однозначный момент: только ISO с T и смещением/Z. 
    public static Instant parseEventInstant(String isoWithOffset) {
        return OffsetDateTime.parse(isoWithOffset, ISO_T_WITH_OFFSET).toInstant();
    }

    // Парсит локальное «стенное» время и привязывает к зоне с фиксированной DST-политикой. 
    public static Instant parseScheduleLocal(String localIsoT, ZoneId zone) {
        LocalDateTime ldt = LocalDateTime.parse(localIsoT, LOCAL_T);
        return resolveDstFixed(ldt, zone).toInstant();
    }

    /**
     * Фиксированная DST-политика:
     * - GAP (весенняя «дыра»)   -> сдвиг вперёд к первому валидному моменту
     * - OVERLAP (осеннее «двойное время») -> берём поздний оффсет
     */
    private static ZonedDateTime resolveDstFixed(LocalDateTime ldt, ZoneId zone) {
        ZoneRules rules = zone.getRules();
        var offsets = rules.getValidOffsets(ldt);

        if (offsets.size() == 1) {
            return ZonedDateTime.of(ldt, zone, offsets.get(0)); // обычный случай
        }
        if (offsets.isEmpty()) { // GAP: времени не существует
            ZoneOffsetTransition tr = rules.getTransition(ldt);
            if (tr == null) throw new DateTimeException("DST gap: " + ldt + " in " + zone);
            // Политика: SHIFT_FWD_TO_VALID
            return ZonedDateTime.of(tr.getDateTimeAfter(), zone);
        }
        // OVERLAP: двусмысленное время — берём поздний оффсет (LATER_OFFSET)
        return ZonedDateTime
                .ofLocal(ldt, zone, offsets.get(1))
                .withLaterOffsetAtOverlap();
    }
}
