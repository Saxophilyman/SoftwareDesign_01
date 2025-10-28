public class AverageCalculator {

    /**
     массив инициализирован,
     его длина должна быть больше нуля (не пустой),
     возвращаем double, 
     используется long, чтобы избежать переполнения int.
     */
    public double calculateAverage(int[] numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("numbers == null");
        }
        if (numbers.length == 0) {
            throw new IllegalArgumentException("numbers is empty");
        }

        long sum = 0L;  
        for (int v : numbers) {
            sum += v;
        }
        return (double) sum / numbers.length;  
    }
}

  
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AverageCalculatorTest {
    //Обычные варианта теста
    @Test
    void averageSimple() {
        AverageCalculator calc = new AverageCalculator();
        assertEquals(3.0,  calc.calculateAverage(new int[]{2, 4}), 1e-9);
        assertEquals(25.0, calc.calculateAverage(new int[]{10, 20, 30, 40}), 1e-9);
        assertEquals(5.0,  calc.calculateAverage(new int[]{5, 5, 5, 5}), 1e-9);
    }

   /**
    если бы в методе не использовался бы лонг,
    то в отсутствии теста возникала бы ошибка: 
    при суммировании скрывалась бы ошибка переполнения int
    */
    @Test
    void bigValues() {
        AverageCalculator calc = new AverageCalculator();
        int[] input = {Integer.MAX_VALUE, Integer.MAX_VALUE};
        assertEquals((double) Integer.MAX_VALUE, calc.calculateAverage(input), 1e-9);
    }
}

    
