// Имеется класс GradeCalculator с методом calculateAverage(List grades), который вычисляет среднее значение оценок студентов.
// Реализуйте этот класс с учётом того, что существуют граничные случаи, которые необходимо правильно обработать, чтобы избежать логических ошибок.

public class GradeCalculator {
    /**
     Какие краевые случаи могут возникнуть?
     Стандартный List не инициализирован или пустой,
     оценка меньше 1 или больше 5 (берём стандартную шкалу оценок),
     сама оценка null
     ---
     внутри списка находятся целочисленные значения Integer
     */
    public double calculateAverage(List<Integer> grades){
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }

        double sum = 0;
        for (Integer mark : grades) {
            if (mark == null) {
                throw new IllegalArgumentException("mark is null");
            }
            if (mark < 0 || mark > 5) {
                throw new IllegalArgumentException("Marks cannot be < 1 or > 5");
            }
            sum += mark;
        }
        return sum / grades.size();
    }
}


import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
//Сделайте пять концептуально различающихся тестов для метода calculateAverage.
public class GradeCalculatorTest {
    //1. Пустой список
    @Test
    void testGradeCalculateListIsEmpty() {
        GradeCalculator result = new GradeCalculator();
        assertThrows(IllegalArgumentException.class, () -> result.calculateAverage(List.of()));
    }

    //2.List is null
    @Test
    void testGradeCalculateListIsNull() {
        GradeCalculator result = new GradeCalculator();
        assertThrows(IllegalArgumentException.class, () -> result.calculateAverage(null));
    }

    //3. Значения оценок в допустимых границах
    @Test
    void testGradeCalculateOutOfBounds() {
        GradeCalculator result = new GradeCalculator();
        // допустимые границы включены
        assertEquals(5.0, result.calculateAverage(List.of(5, 5)), 1e-9);
        // выход за границы
        assertThrows(IllegalArgumentException.class, () -> result.calculateAverage(List.of(-1, 50)));
        assertThrows(IllegalArgumentException.class, () -> result.calculateAverage(List.of(6, 4)));
    }

    //4. Оценка null
    @Test
    void testGradeCalculateMarkIsNull() {
        GradeCalculator result = new GradeCalculator();
        assertThrows(IllegalArgumentException.class,
                () -> result.calculateAverage(Arrays.asList(3, null, 5)));
    }

    //5. Проверка на 1 элемент, число должно равняться самому себе
    @Test
    void testGradeCalculateSingleReturnItself() {
        GradeCalculator result = new GradeCalculator();
        assertEquals(4.0, result.calculateAverage(List.of(4)), 1e-9);
    }

    //6. Проверка корректности дробного результата
    @Test
    void testGradeCalculateFractionStayRight() {
        GradeCalculator result = new GradeCalculator();
        assertEquals(1.5,  result.calculateAverage(List.of(1, 2)), 1e-9);
    }
}
